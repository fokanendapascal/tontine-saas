package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.SavingsRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsResponse;
import com.techsolution.tontine_saas.entities.Savings;
import com.techsolution.tontine_saas.entities.TransactionType;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.SavingsMapper;
import com.techsolution.tontine_saas.repository.SavingsRepository;
import com.techsolution.tontine_saas.repository.SavingsTransactionRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.SavingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsServiceImpl implements SavingsService {

    private final SavingsRepository savingsRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final SavingsTransactionRepository transactionRepository;

    // Seuil d'éligibilité (peut être rendu dynamique par association plus tard)
    private static final BigDecimal LOAN_THRESHOLD = new BigDecimal("100000");

    @Override
    @Transactional
    public SavingsResponse createSavingsAccount(SavingsRequest request) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        // 1. Vérification si le compte existe déjà
        if (savingsRepository.findByUser_Id(request.getUserId()).isPresent()) {
            throw new BaseException("savings.already.exists", "SAVINGS_EXISTS", HttpStatus.BAD_REQUEST);
        }

        // 2. Récupération de l'utilisateur et vérification du Tenant (Multi-tenancy)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!user.getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_ASSOCIATION", HttpStatus.FORBIDDEN);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 3. Création
        Savings savings = SavingsMapper.toEntity(request, user);
        Savings saved = savingsRepository.save(savings);

        auditLogService.logAction("CREATE_SAVINGS", "Savings", saved.getId(), admin);
        return enrichResponse(saved);
    }

    @Override
    @Transactional
    public SavingsResponse deposit(Long userId, BigDecimal amount) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException("invalid.amount", "INVALID_AMOUNT", HttpStatus.BAD_REQUEST);
        }

        // Verrouillage Pessimiste pour éviter les "Race Conditions" sur le solde
        Savings savings = savingsRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new BaseException("savings.not.found", "SAVINGS_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Vérification de sécurité : l'admin doit appartenir à la même asso que le compte
        validateAssociationAccess(savings, associationId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        savings.setBalance(savings.getBalance().add(amount));
        Savings updated = savingsRepository.save(savings);

        auditLogService.logAction("SAVINGS_DEPOSIT", "Savings", updated.getId(), admin);
        return enrichResponse(updated);
    }

    @Override
    @Transactional
    public SavingsResponse withdraw(Long userId, BigDecimal amount) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        Savings savings = savingsRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new BaseException("savings.not.found", "SAVINGS_NOT_FOUND", HttpStatus.NOT_FOUND));

        validateAssociationAccess(savings, associationId);

        if (savings.getBalance().compareTo(amount) < 0) {
            throw new BaseException("insufficient.balance", "INSUFFICIENT_FUNDS", HttpStatus.BAD_REQUEST);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        savings.setBalance(savings.getBalance().subtract(amount));
        Savings updated = savingsRepository.save(savings);

        auditLogService.logAction("SAVINGS_WITHDRAWAL", "Savings", updated.getId(), admin);
        return enrichResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsResponse getSavingsByUserId(Long userId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        Savings savings = savingsRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BaseException("savings.not.found", "SAVINGS_NOT_FOUND", HttpStatus.NOT_FOUND));

        validateAssociationAccess(savings, associationId);

        return enrichResponse(savings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsResponse> getAssociationSavings() {
        // L'ID de l'association est extrait du token, plus besoin de paramètre
        Long associationId = SecurityUtils.getCurrentAssociationId();

        return savingsRepository.findByUser_Association_Id(associationId).stream()
                .map(this::enrichResponse)
                .toList();
    }

    /**
     * Helper de validation Multi-tenant
     */
    private void validateAssociationAccess(Savings savings, Long currentAssociationId) {
        if (!savings.getUser().getAssociation().getId().equals(currentAssociationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_CROSS_TENANT", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Helper pour enrichir la réponse avec les statistiques réelles
     */
    private SavingsResponse enrichResponse(Savings savings) {
        // Ici on pourrait compter les transactions réelles via transactionRepository
        Long totalDeposits = transactionRepository.countByTypeAndSavingsId(TransactionType.DEPOSIT, savings.getId());
        Long totalWithdrawals = transactionRepository.countByTypeAndSavingsId(TransactionType.WITHDRAWAL, savings.getId());

        return SavingsMapper.toResponse(
                savings,
                totalDeposits,
                totalWithdrawals,
                savings.getBalance(),
                LOAN_THRESHOLD
        );
    }

}
