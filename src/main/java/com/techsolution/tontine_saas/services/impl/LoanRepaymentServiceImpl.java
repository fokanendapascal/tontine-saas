package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.LoanRepaymentRequest;
import com.techsolution.tontine_saas.dtos.response.LoanRepaymentResponse;
import com.techsolution.tontine_saas.entities.Loan;
import com.techsolution.tontine_saas.entities.LoanRepayment;
import com.techsolution.tontine_saas.entities.LoanStatus;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.LoanRepaymentMapper;
import com.techsolution.tontine_saas.repository.LoanRepaymentRepository;
import com.techsolution.tontine_saas.repository.LoanRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.LoanRepaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanRepaymentServiceImpl implements LoanRepaymentService {

    private final LoanRepaymentRepository repaymentRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public LoanRepaymentResponse recordRepayment(LoanRepaymentRequest request, Long adminId) {
        // 1. Récupérer le prêt et l'admin
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new BaseException("loan.not.found", "LOAN_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("user.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 2. Vérifier si le prêt est déjà remboursé ou non approuvé
        if (loan.getStatus() != LoanStatus.APPROVED && loan.getStatus() != LoanStatus.PARTIALLY_PAID) {
            throw new BaseException("loan.invalid.status", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
        }

        // 3. Créer et enregistrer le remboursement
        LoanRepayment repayment = LoanRepaymentMapper.toEntity(request, loan);
        LoanRepayment savedRepayment = repaymentRepository.save(repayment);

        // 4. Calculer le nouveau total remboursé pour mettre à jour le statut du prêt
        BigDecimal totalRepaid = repaymentRepository.sumAmountByLoanId(loan.getId());

        updateLoanStatus(loan, totalRepaid);

        // 5. Audit
        auditLogService.logAction("RECORD_REPAYMENT", "LoanRepayment", savedRepayment.getId(), admin);

        return LoanRepaymentMapper.toResponse(savedRepayment, totalRepaid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> getRepaymentsByLoan(Long loanId) {
        return repaymentRepository.findByLoanId(loanId).stream()
                .map(r -> {
                    BigDecimal currentTotal = repaymentRepository.sumAmountByLoanId(loanId);
                    return LoanRepaymentMapper.toResponse(r, currentTotal);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanRepaymentResponse getRepaymentById(Long id) {
        LoanRepayment r = repaymentRepository.findById(id)
                .orElseThrow(() -> new BaseException("repayment.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        BigDecimal totalAtThatTime = repaymentRepository.sumAmountByLoanId(r.getLoan().getId());
        return LoanRepaymentMapper.toResponse(r, totalAtThatTime);
    }

    /**
     * Logique de mise à jour du statut du prêt selon le montant remboursé
     */
    private void updateLoanStatus(Loan loan, BigDecimal totalRepaid) {
        if (totalRepaid.compareTo(loan.getAmount()) >= 0) {
            loan.setStatus(LoanStatus.PAID);
        } else if (totalRepaid.compareTo(BigDecimal.ZERO) > 0) {
            loan.setStatus(LoanStatus.PARTIALLY_PAID);
        }
        loanRepository.save(loan);
    }
}
