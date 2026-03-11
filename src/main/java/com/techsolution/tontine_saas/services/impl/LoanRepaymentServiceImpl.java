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
import com.techsolution.tontine_saas.security.SecurityUtils;
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
    public LoanRepaymentResponse recordRepayment(LoanRepaymentRequest request) {
        // 1. Identification via le SecurityContext
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        // 2. Récupérer le prêt avec verrouillage (Pessimistic Lock pour éviter les accès concurrents sur le solde)
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new BaseException("loan.not.found", "LOAN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Sécurité Multi-tenant
        if (!loan.getUser().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_ASSOCIATION_ACCESS", HttpStatus.FORBIDDEN);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("user.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 3. Vérifier la validité du statut et du montant
        if (loan.getStatus() != LoanStatus.APPROVED && loan.getStatus() != LoanStatus.PARTIALLY_PAID) {
            throw new BaseException("loan.invalid.status", "LOAN_NOT_ACTIVE", HttpStatus.BAD_REQUEST);
        }

        BigDecimal amountToPay = request.getAmount();
        if (amountToPay.compareTo(loan.getRemainingAmount()) > 0) {
            throw new BaseException("repayment.overflow", "AMOUNT_EXCEEDS_REMAINING_DEBT", HttpStatus.BAD_REQUEST);
        }

        // 4. Déduction automatique du montant restant et mise à jour du statut
        loan.setRemainingAmount(loan.getRemainingAmount().subtract(amountToPay));

        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.PAID);
        } else {
            loan.setStatus(LoanStatus.PARTIALLY_PAID);
        }
        loanRepository.save(loan);

        // 5. Enregistrer le remboursement
        LoanRepayment repayment = LoanRepaymentMapper.toEntity(request, loan);
        LoanRepayment savedRepayment = repaymentRepository.save(repayment);

        // 6. Audit et réponse
        auditLogService.logAction("RECORD_REPAYMENT", "LoanRepayment", savedRepayment.getId(), admin);

        // Calcul du total remboursé pour la réponse (Somme des remboursements)
        BigDecimal totalRepaidSoFar = repaymentRepository.sumAmountByLoanId(loan.getId());
        return LoanRepaymentMapper.toResponse(savedRepayment, totalRepaidSoFar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> getRepaymentsByLoan(Long loanId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BaseException("loan.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!loan.getUser().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "UNAUTHORIZED", HttpStatus.FORBIDDEN);
        }

        BigDecimal totalRepaid = repaymentRepository.sumAmountByLoanId(loanId);
        if (totalRepaid == null) totalRepaid = BigDecimal.ZERO;

        final BigDecimal finalTotal = totalRepaid;
        return repaymentRepository.findByLoanId(loanId).stream()
                .map(r -> LoanRepaymentMapper.toResponse(r, finalTotal))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanRepaymentResponse getRepaymentById(Long id) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        LoanRepayment r = repaymentRepository.findById(id)
                .orElseThrow(() -> new BaseException("repayment.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!r.getLoan().getUser().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "UNAUTHORIZED", HttpStatus.FORBIDDEN);
        }

        BigDecimal totalAtThatTime = repaymentRepository.sumAmountByLoanId(r.getLoan().getId());
        return LoanRepaymentMapper.toResponse(r, totalAtThatTime);
    }

}
