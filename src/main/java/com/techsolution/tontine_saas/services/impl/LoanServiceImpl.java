package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.LoanRequest;
import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.LoanResponse;
import com.techsolution.tontine_saas.entities.Loan;
import com.techsolution.tontine_saas.entities.LoanStatus;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.LoanMapper;
import com.techsolution.tontine_saas.repository.LoanRepaymentRepository;
import com.techsolution.tontine_saas.repository.LoanRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final LoanRepaymentRepository loanrepaymentRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public LoanResponse createLoanRequest(LoanRequest loanRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Conversion DTO -> Entity via le Mapper
        Loan loan = LoanMapper.toEntity(loanRequest, user);
        loan.setStatus(LoanStatus.PENDING);

        Loan savedLoan = loanRepository.save(loan);

        auditLogService.logAction("CREATE_LOAN_REQUEST", "Loan", savedLoan.getId(), user);

        return convertToResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse approveLoan(Long loanId, UserRequest adminRequest) {
        Loan loan = findLoanById(loanId);

        // Correction : on lance une exception métier, pas le Handler !
        User admin = userRepository.findByEmail(adminRequest.getEmail())
                .orElseThrow(() -> new BaseException(
                        "user.not.found",
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND,
                        adminRequest.getEmail()
                ));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BaseException(
                    "loan.invalid.status",
                    "INVALID_LOAN_STATUS",
                    HttpStatus.BAD_REQUEST
            );
        }

        loan.setStatus(LoanStatus.APPROVED);
        Loan updatedLoan = loanRepository.save(loan);

        auditLogService.logAction("APPROVE_LOAN", "Loan", updatedLoan.getId(), admin);

        return convertToResponse(updatedLoan);
    }

    @Override
    @Transactional
    public LoanResponse rejectLoan(Long loanId, UserRequest adminRequest) {
        Loan loan = findLoanById(loanId);

        User admin = userRepository.findByEmail(adminRequest.getEmail())
                .orElseThrow(() -> new BaseException(
                        "user.not.found",
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND,
                        adminRequest.getEmail()
                ) );

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Impossible de rejeter un prêt déjà traité.");
        }

        loan.setStatus(LoanStatus.REJECTED);
        Loan updatedLoan = loanRepository.save(loan);

        auditLogService.logAction("REJECT_LOAN", "Loan", updatedLoan.getId(), admin);

        return convertToResponse(updatedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getUserLoans(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getAssociationLoans(Long associationId) {
        return loanRepository.findByUser_Association_Id(associationId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        return convertToResponse(findLoanById(loanId));
    }

    // --- Helpers ---

    private Loan findLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prêt introuvable ID: " + id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable ID: " + id));
    }

    private LoanResponse convertToResponse(Loan loan) {
        // On récupère dynamiquement la somme des remboursements pour enrichir le DTO
        BigDecimal totalRepaid = loanrepaymentRepository.sumAmountByLoanId(loan.getId());
        return LoanMapper.toResponse(loan, totalRepaid);
    }
}