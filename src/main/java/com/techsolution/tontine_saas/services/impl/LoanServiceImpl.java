package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.LoanRequest;
import com.techsolution.tontine_saas.dtos.response.LoanResponse;
import com.techsolution.tontine_saas.entities.Loan;
import com.techsolution.tontine_saas.entities.LoanStatus;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.LoanMapper;
import com.techsolution.tontine_saas.repository.LoanRepaymentRepository;
import com.techsolution.tontine_saas.repository.LoanRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public LoanResponse createLoanRequest(LoanRequest loanRequest) {
        // L'ID utilisateur est celui de la personne connectée (le demandeur)
        Long userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Conversion DTO -> Entity
        Loan loan = LoanMapper.toEntity(loanRequest, user);
        loan.setStatus(LoanStatus.PENDING);

        // Initialisation des montants par défaut si nécessaire
        loan.setRemainingAmount(loan.calculateInitialTotal());

        Loan savedLoan = loanRepository.save(loan);

        auditLogService.logAction("CREATE_LOAN_REQUEST", "Loan", savedLoan.getId(), user);

        return convertToResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse approveLoan(Long loanId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        Loan loan = findLoanById(loanId);

        // Sécurité Multi-tenant : Vérifier que le prêt appartient à l'association de l'admin
        validateAssociationAccess(loan, associationId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BaseException("loan.invalid.status", "INVALID_LOAN_STATUS", HttpStatus.BAD_REQUEST);
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setStartDate(java.time.LocalDate.now());

        Loan updatedLoan = loanRepository.save(loan);

        auditLogService.logAction("APPROVE_LOAN", "Loan", updatedLoan.getId(), admin);

        return convertToResponse(updatedLoan);
    }

    @Override
    @Transactional
    public LoanResponse rejectLoan(Long loanId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        Loan loan = findLoanById(loanId);
        validateAssociationAccess(loan, associationId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BaseException("loan.invalid.status", "CANNOT_REJECT_PROCESSED_LOAN", HttpStatus.BAD_REQUEST);
        }

        loan.setStatus(LoanStatus.REJECTED);
        Loan updatedLoan = loanRepository.save(loan);

        auditLogService.logAction("REJECT_LOAN", "Loan", updatedLoan.getId(), admin);

        return convertToResponse(updatedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getUserLoans() {
        Long userId = SecurityUtils.getCurrentUserId();
        return loanRepository.findByUser_Id(userId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getAssociationLoans() {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        return loanRepository.findByUser_Association_Id(associationId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Loan loan = findLoanById(loanId);

        validateAssociationAccess(loan, associationId);

        return convertToResponse(loan);
    }

    // --- Helpers ---
    private void validateAssociationAccess(Loan loan, Long associationId) {
        if (!loan.getUser().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_LOAN_ACCESS", HttpStatus.FORBIDDEN);
        }
    }

    private Loan findLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new BaseException("loan.not.found", "LOAN_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private LoanResponse convertToResponse(Loan loan) {
        BigDecimal totalRepaid = loanRepaymentRepository.sumAmountByLoanId(loan.getId());
        if (totalRepaid == null) totalRepaid = BigDecimal.ZERO;

        return LoanMapper.toResponse(loan, totalRepaid);
    }

}