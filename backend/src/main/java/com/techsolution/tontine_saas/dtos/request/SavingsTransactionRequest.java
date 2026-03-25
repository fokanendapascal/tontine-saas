package com.techsolution.tontine_saas.dtos.request;

import com.techsolution.tontine_saas.entities.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsTransactionRequest {

    @NotNull(message = "Savings ID is required")
    private Long savingsId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    // DEPOSIT / WITHDRAWAL / TRANSFER (si applicable)

    private String description;

    // 🔹 Extensions intelligentes
    @Builder.Default
    private Boolean autoUpdateBalance = true;

    @Builder.Default
    private Boolean validateSufficientBalance = true;
}