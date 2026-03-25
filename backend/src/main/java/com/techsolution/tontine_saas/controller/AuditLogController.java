package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.response.AuditLogResponse;
import com.techsolution.tontine_saas.services.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "API de traçabilité et historique des actions administratives")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Historique d'un utilisateur",
            description = "Récupère toutes les actions effectuées par un utilisateur (admin ou membre).")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getUserHistory(userId));
    }

    @Operation(summary = "Logs d'une association",
            description = "Permet aux super-admins de voir tout ce qui s'est passé au sein d'une association spécifique.")
    @GetMapping("/association/{associationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getAssociationLogs(@PathVariable Long associationId) {
        return ResponseEntity.ok(auditLogService.getAssociationLogs(associationId));
    }

    @Operation(summary = "Historique d'une entité spécifique",
            description = "Récupère les modifications subies par une ressource (ex: toutes les actions sur le Prêt n°45).")
    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getEntityHistory(
            @PathVariable String entityName,
            @PathVariable Long entityId
    ) {
        return ResponseEntity.ok(auditLogService.getEntityHistory(entityName, entityId));
    }
}
