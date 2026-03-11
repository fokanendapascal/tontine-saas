package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.LoanRequest;
import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.LoanResponse;

import java.util.List;

public interface LoanService {

    LoanResponse createLoanRequest(LoanRequest loanRequest);

    LoanResponse approveLoan(Long loanId);

    LoanResponse rejectLoan(Long loanId);

    List<LoanResponse> getUserLoans();

    List<LoanResponse> getAssociationLoans();

    LoanResponse getLoanById(Long loanId);

}
