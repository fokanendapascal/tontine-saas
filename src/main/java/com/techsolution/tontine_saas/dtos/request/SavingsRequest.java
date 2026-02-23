package com.techsolution.tontine_saas.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsRequest {

    @NotNull(message = "Balance is required")
    @PositiveOrZero(message = "Balance must be positive or zero")
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull(message = "User ID is required")
    private Long userId;

    // 🔹 Extension intelligente
    @Builder.Default
    private Boolean autoInitialize = true;
}