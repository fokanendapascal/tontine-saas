package com.techsolution.tontine_saas.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDate paymentDate;

    @PositiveOrZero(message = "Penalty must be zero or positive")
    @Builder.Default
    private BigDecimal penalty = BigDecimal.ZERO;

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    // 🔹 Extension intelligente
    @Builder.Default
    private Boolean autoApplyPenalty = true;
}