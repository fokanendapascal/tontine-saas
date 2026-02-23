package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.LoanRepaymentRequest;
import com.techsolution.tontine_saas.dtos.response.LoanRepaymentResponse;
import com.techsolution.tontine_saas.entities.Loan;
import com.techsolution.tontine_saas.entities.LoanRepayment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanRepaymentMapper {

    public static LoanRepayment toEntity(LoanRepaymentRequest request, Loan loan) {

        return LoanRepayment.builder()
                .amount(request.getAmount())
                .paymentDate(
                        request.getPaymentDate() != null
                                ? request.getPaymentDate()
                                : LocalDate.now()
                )
                .penalty(
                        request.getPenalty() != null
                                ? request.getPenalty()
                                : BigDecimal.ZERO
                )
                .loan(loan)
                .build();
    }

    public static LoanRepaymentResponse toResponse(
            LoanRepayment repayment,
            BigDecimal totalRepaidAfterThisPayment
    ) {

        Loan loan = repayment.getLoan();

        BigDecimal totalLoanAmount =
                loan != null && loan.getAmount() != null
                        ? loan.getAmount()
                        : BigDecimal.ZERO;

        BigDecimal remaining =
                totalLoanAmount.subtract(
                        totalRepaidAfterThisPayment != null
                                ? totalRepaidAfterThisPayment
                                : BigDecimal.ZERO
                );

        LoanRepaymentResponse.LoanRepaymentResponseBuilder builder =
                LoanRepaymentResponse.builder()
                        .id(repayment.getId())
                        .amount(repayment.getAmount())
                        .paymentDate(repayment.getPaymentDate())
                        .penalty(repayment.getPenalty())
                        .loanId(loan != null ? loan.getId() : null)
                        .loanStatus(
                                loan != null && loan.getStatus() != null
                                        ? loan.getStatus().name()
                                        : null
                        )
                        .totalLoanAmount(totalLoanAmount)
                        .remainingAfterPayment(remaining);

        if (loan != null && loan.getUser() != null) {
            builder.borrowerFullName(
                    loan.getUser().getFirstName() + " " +
                            loan.getUser().getLastName()
            );
        }

        return builder.build();
    }
}