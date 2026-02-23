package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.AuditLogRequest;
import com.techsolution.tontine_saas.dtos.response.AuditLogResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.AuditLog;

public class AuditLogMapper {

    public static AuditLog toEntity(AuditLogRequest request, Association association) {
        return AuditLog.builder()
                .action(request.getAction())
                .entityName(request.getEntityName())
                .entityId(request.getEntityId())
                .performedBy(request.getPerformedBy())
                .association(association)
                .build();
    }

    public static AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .performedAt(log.getPerformedAt())
                .associationId(log.getAssociation() != null ? log.getAssociation().getId() : null)
                .associationName(log.getAssociation() != null ? log.getAssociation().getName() : null)
                .performedByFullName(
                        log.getPerformedBy() != null
                                ? "User#" + log.getPerformedBy()
                                : null
                )
                .entityDisplayName(log.getEntityName()) // peut être enrichi selon type d'entité
                .build();
    }
}