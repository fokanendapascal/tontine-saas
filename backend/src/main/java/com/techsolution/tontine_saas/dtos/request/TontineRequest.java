package com.techsolution.tontine_saas.dtos.request;

import com.techsolution.tontine_saas.entities.TontineFrequency;
import jakarta.validation.constraints.NotBlank;
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
public class TontineRequest {

    @NotBlank(message = "Tontine name is required")
    private String name;

    @NotNull(message = "Contribution amount is required")
    @Positive(message = "Contribution amount must be positive")
    private BigDecimal contributionAmount;

    @NotBlank(message = "Frequency is required")
    private TontineFrequency frequency;

    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private Boolean active = true;

    @NotNull(message = "Association ID is required")
    private Long associationId;

    // 🔹 Extension intelligente
    @Builder.Default
    private Boolean autoGenerateSchedule = true;
}
