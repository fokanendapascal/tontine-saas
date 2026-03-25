package com.techsolution.tontine_saas.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberTontineRequest {

    @Positive(message = "Order position must be positive")
    private Integer orderPosition;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Tontine ID is required")
    private Long tontineId;

    // 🔹 Extension intelligente
    @Builder.Default
    private Boolean autoAssignOrder = false;
}