package com.techsolution.tontine_saas.dtos.request;

import com.techsolution.tontine_saas.entities.ContributionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDate dueDate;

    private LocalDate paymentDate;

    private BigDecimal penalty;

    @NotNull(message = "Status is required")
    private ContributionStatus status;

    @NotNull(message = "MemberTontine ID is required")
    private Long memberTontineId;

    // 🔹 Extensions intelligentes pour audit / validation
    @Builder.Default
    private Boolean autoCalculatePenalty = true; // si true, le service peut recalculer la pénalité
}