package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.response.AuditLogResponse;
import com.techsolution.tontine_saas.entities.AuditLog;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.mappers.AuditLogMapper;
import com.techsolution.tontine_saas.repository.AuditLogRepository;
import com.techsolution.tontine_saas.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityName, Long entityId, User performer) {
        // Construction de l'entité via le Builder (ou un AuditLogRequest temporaire si vous préférez)
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedBy(performer.getId())
                .association(performer.getAssociation())
                .build();

        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getUserHistory(Long userId) {
        return auditLogRepository.findByPerformedByOrderByPerformedAtDesc(userId).stream()
                .map(AuditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAssociationLogs(Long associationId) {
        return auditLogRepository.findByAssociationIdOrderByPerformedAtDesc(associationId).stream()
                .map(AuditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getEntityHistory(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByPerformedAtDesc(entityName, entityId).stream()
                .map(AuditLogMapper::toResponse)
                .collect(Collectors.toList());
    }
}
