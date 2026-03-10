package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.SavingsTransactionRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsTransactionResponse;
import com.techsolution.tontine_saas.entities.Savings;
import com.techsolution.tontine_saas.entities.SavingsTransaction;
import com.techsolution.tontine_saas.entities.TransactionType;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.repository.SavingsRepository;
import com.techsolution.tontine_saas.repository.SavingsTransactionRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.SavingsTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsTransactionServiceImpl implements SavingsTransactionService {

    private final SavingsTransactionRepository transactionRepository;
    private final SavingsRepository savingsRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public SavingsTransactionResponse processTransaction(SavingsTransactionRequest request) {
        // 1. Identification via le SecurityContext
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        // 2. Récupérer le compte avec verrouillage et vérifier l'association
        Savings savings = savingsRepository.findById(request.getSavingsId())
                .orElseThrow(() -> new BaseException("savings.not.found", "SAVINGS_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Sécurité Multi-tenant : Le compte épargne doit appartenir à l'association de l'admin
        if (!savings.getUser().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_ASSOCIATION_ACCESS", HttpStatus.FORBIDDEN);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        BigDecimal previousBalance = savings.getBalance();
        BigDecimal amount = request.getAmount();
        BigDecimal balanceAfter = previousBalance;

        // 3. Validation et mise à jour du solde
        if (request.getType() == TransactionType.WITHDRAWAL) {
            if (previousBalance.compareTo(amount) < 0 && Boolean.TRUE.equals(request.getValidateSufficientBalance())) {
                throw new BaseException("insufficient.balance", "INSUFFICIENT_FUNDS", HttpStatus.BAD_REQUEST);
            }
            balanceAfter = previousBalance.subtract(amount);
        } else if (request.getType() == TransactionType.DEPOSIT) {
            balanceAfter = previousBalance.add(amount);
        }

        // 4. Persistance du nouveau solde sur le compte épargne
        if (Boolean.TRUE.equals(request.getAutoUpdateBalance())) {
            savings.setBalance(balanceAfter);
            savingsRepository.save(savings);
        }

        // 5. Création de l'entité Transaction avec les bonnes valeurs historiques
        SavingsTransaction transaction = SavingsTransaction.builder()
                .amount(amount)
                .type(request.getType())
                .description(request.getDescription())
                .previousBalance(previousBalance)
                .balanceAfterTransaction(balanceAfter) // Corrigé : utilise la valeur après calcul
                .savings(savings)
                .successful(true)
                .build();

        SavingsTransaction saved = transactionRepository.save(transaction);

        // 6. Audit
        auditLogService.logAction("SAVINGS_" + request.getType().name(), "SavingsTransaction", saved.getId(), admin);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsTransactionResponse> getTransactionHistory(Long savingsId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        // Vérifier que le compte épargne appartient à l'association
        Savings savings = savingsRepository.findById(savingsId)
                .orElseThrow(() -> new BaseException("savings.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!savings.getUser().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "UNAUTHORIZED", HttpStatus.FORBIDDEN);
        }

        return transactionRepository.findBySavingsIdOrderByCreatedAtDesc(savingsId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsTransactionResponse getTransactionById(Long id) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        SavingsTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BaseException("transaction.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!transaction.getSavings().getUser().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "UNAUTHORIZED", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(transaction);
    }

    private SavingsTransactionResponse mapToResponse(SavingsTransaction t) {
        User user = t.getSavings().getUser();
        return SavingsTransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .savingsId(t.getSavings().getId())
                .previousBalance(t.getPreviousBalance())
                .balanceAfterTransaction(t.getBalanceAfterTransaction())
                .userFullName(user.getFirstName() + " " + user.getLastName())
                .successful(t.getSuccessful())
                .build();
    }

}
