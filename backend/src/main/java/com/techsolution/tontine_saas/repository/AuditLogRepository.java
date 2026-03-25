package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Récupérer l'historique d'un utilisateur du plus récent au plus ancien
    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(Long userId);

    // Récupérer tous les logs d'une association
    List<AuditLog> findByAssociationIdOrderByPerformedAtDesc(Long associationId);

    // Utile pour voir l'historique spécifique d'un objet (ex: toutes les actions sur le Prêt n°5)
    List<AuditLog> findByEntityNameAndEntityIdOrderByPerformedAtDesc(String entityName, Long entityId);

}
