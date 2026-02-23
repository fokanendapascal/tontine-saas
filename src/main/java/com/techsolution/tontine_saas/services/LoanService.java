package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.LoanRequest;
import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.LoanResponse;

import java.util.List;

public interface LoanService {

    LoanResponse createLoanRequest(LoanRequest loanRequest, Long userId);

    LoanResponse approveLoan(Long loanId, UserRequest adminRequest);

    LoanResponse rejectLoan(Long loanId, UserRequest adminRequest);

    List<LoanResponse> getUserLoans(Long userId);

    List<LoanResponse> getAssociationLoans(Long associationId);

    LoanResponse getLoanById(Long loanId);

}
