package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.response.AuditLogResponse;
import com.techsolution.tontine_saas.entities.AuditLog;
import com.techsolution.tontine_saas.entities.User;

import java.util.List;

public interface AuditLogService {

    // Enregistre un log de manière simple
    void logAction(String action, String entityName, Long entityId, User performer);

    // Récupère les logs par utilisateur
    List<AuditLogResponse> getUserHistory(Long userId);

    // Récupère les logs d'une association
    List<AuditLogResponse> getAssociationLogs(Long associationId);

    // Récupère l'historique d'une entité spécifique (ex: un prêt précis)
    List<AuditLogResponse> getEntityHistory(String entityName, Long entityId);
}
