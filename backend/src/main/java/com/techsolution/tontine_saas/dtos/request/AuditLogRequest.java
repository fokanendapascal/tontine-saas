package com.techsolution.tontine_saas.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogRequest {

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Entity name is required")
    private String entityName;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    @NotNull(message = "PerformedBy user ID is required")
    private Long performedBy;

    @NotNull(message = "Association ID is required")
    private Long associationId;
}
