package com.techsolution.tontine_saas.dtos.request;

import com.techsolution.tontine_saas.entities.LoanStatus;
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
public class LoanRequest {

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    private BigDecimal amount;

    @PositiveOrZero(message = "Interest rate must be positive or zero")
    private Double interestRate;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Loan status is required")
    private LoanStatus status;

    @NotNull(message = "User ID is required")
    private Long userId;

    // 🔹 Extension intelligente
    @Builder.Default
    private Boolean autoCalculateInterest = true;
}