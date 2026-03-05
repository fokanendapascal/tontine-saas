package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.SavingsRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsResponse;
import com.techsolution.tontine_saas.entities.Savings;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.SavingsMapper;
import com.techsolution.tontine_saas.repository.SavingsRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
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

    // Seuil d'éligibilité (peut être rendu dynamique par association plus tard)
    private static final BigDecimal LOAN_THRESHOLD = new BigDecimal("100000");

    @Override
    @Transactional
    public SavingsResponse createSavingsAccount(SavingsRequest request, Long adminId) {
        if (savingsRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BaseException("savings.already.exists", "SAVINGS_EXISTS", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        Savings savings = SavingsMapper.toEntity(request, user);
        Savings saved = savingsRepository.save(savings);

        auditLogService.logAction("CREATE_SAVINGS", "Savings", saved.getId(), admin);
        return enrichResponse(saved);
    }

    @Override
    @Transactional
    public SavingsResponse deposit(Long userId, BigDecimal amount, Long adminId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException("invalid.amount", "INVALID_AMOUNT", HttpStatus.BAD_REQUEST);
        }

        // Verrouillage de la ligne en base pour éviter les accès concurrents
        Savings savings = savingsRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new BaseException("savings.not.found", "SAVINGS_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        savings.setBalance(savings.getBalance().add(amount));
        Savings updated = savingsRepository.save(savings);

        auditLogService.logAction("SAVINGS_DEPOSIT", "Savings", updated.getId(), admin);
        return enrichResponse(updated);
    }

    @Override
    @Transactional
    public SavingsResponse withdraw(Long userId, BigDecimal amount, Long adminId) {
        Savings savings = savingsRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new BaseException("savings.not.found", "SAVINGS_NOT_FOUND", HttpStatus.NOT_FOUND));

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
        return savingsRepository.findByUserId(userId)
                .map(this::enrichResponse)
                .orElseThrow(() -> new BaseException("savings.not.found", "SAVINGS_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsResponse> getAssociationSavings(Long associationId) {
        return savingsRepository.findByUser_Association_Id(associationId).stream()
                .map(this::enrichResponse)
                .toList();
    }

    /**
     * Helper pour transformer l'entité en DTO enrichi
     */
    private SavingsResponse enrichResponse(Savings savings) {
        // Dans une version complète, ces chiffres viendraient d'un SavingsTransactionRepository
        return SavingsMapper.toResponse(
                savings,
                0L, // totalDeposits
                0L, // totalWithdrawals
                savings.getBalance(),
                LOAN_THRESHOLD
        );
    }
}
