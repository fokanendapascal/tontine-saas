package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.LoanRepaymentRequest;
import com.techsolution.tontine_saas.dtos.response.LoanRepaymentResponse;

import java.util.List;

public interface LoanRepaymentService {

    LoanRepaymentResponse recordRepayment(LoanRepaymentRequest request);

    List<LoanRepaymentResponse> getRepaymentsByLoan(Long loanId);

    LoanRepaymentResponse getRepaymentById(Long id);
}
