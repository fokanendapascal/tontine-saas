package com.techsolution.tontine_saas.dtos.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;

    private String action;

    private String entityName;

    private Long entityId;

    private Long performedBy;

    private LocalDateTime performedAt;

    private Long associationId;

    // 🔥 Extensions intelligentes
    private String performedByFullName;  // prénom + nom de l'utilisateur
    private String associationName;       // nom de l'association
    private String entityDisplayName;     // titre ou info principale de l'entité affectée
}
