package com.techsolution.tontine_saas.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentResponse {

    private Long id;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private BigDecimal penalty;

    private Long loanId;

    // 🔥 Extensions intelligentes
    private String loanStatus;
    private String borrowerFullName;
    private BigDecimal totalLoanAmount;
    private BigDecimal remainingAfterPayment;
}