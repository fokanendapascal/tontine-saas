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
    public SavingsTransactionResponse processTransaction(SavingsTransactionRequest request, Long adminId) {

        // 1. Récupérer le compte épargne avec verrouillage (Pessimistic Write)
        Savings savings = savingsRepository.findById(request.getSavingsId())
                .orElseThrow(() -> new BaseException("savings.not.found", "SAVINGS_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        BigDecimal previousBalance = savings.getBalance();
        BigDecimal amount = request.getAmount();
        BigDecimal after = savings.getBalance();

        // 2. Validation du solde si c'est un retrait
        if (request.getType() == TransactionType.WITHDRAWAL && Boolean.TRUE.equals(request.getValidateSufficientBalance())) {
            if (previousBalance.compareTo(amount) < 0) {
                throw new BaseException("insufficient.balance", "INSUFFICIENT_FUNDS", HttpStatus.BAD_REQUEST);
            }
        }

        // 3. Mise à jour du solde si autoUpdateBalance est vrai
        if (Boolean.TRUE.equals(request.getAutoUpdateBalance())) {
            if (request.getType() == TransactionType.DEPOSIT) {
                savings.setBalance(previousBalance.add(amount));
            } else if (request.getType() == TransactionType.WITHDRAWAL) {
                savings.setBalance(previousBalance.subtract(amount));
            }
            savingsRepository.save(savings);
        }

        // 4. Création de l'entité Transaction
        SavingsTransaction transaction = SavingsTransaction.builder()
                .amount(amount)
                .type(request.getType())
                .description(request.getDescription())
                .previousBalance(previousBalance)
                .balanceAfterTransaction(after)
                .savings(savings)
                .successful(true)
                .build();

        SavingsTransaction saved = transactionRepository.save(transaction);

        // 5. Audit
        auditLogService.logAction(request.getType().name(), "SavingsTransaction", saved.getId(), admin);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsTransactionResponse> getTransactionHistory(Long savingsId) {
        return transactionRepository.findBySavingsIdOrderByCreatedAtDesc(savingsId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsTransactionResponse getTransactionById(Long id) {
        SavingsTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BaseException("transaction.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));
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
