package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.TontineRequest;
import com.techsolution.tontine_saas.dtos.response.TontineResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.Tontine;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.TontineMapper;
import com.techsolution.tontine_saas.repository.*;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.TontineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TontineServiceImpl implements TontineService {

    private final TontineRepository tontineRepository;
    private final AssociationRepository associationRepository;
    private final UserRepository userRepository;
    private final MemberTontineRepository memberTontineRepository;
    private final ContributionRepository contributionRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public TontineResponse createTontine(TontineRequest request) {
        // L'ID de l'association est extrait du JWT : impossible de créer une tontine pour autrui
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new BaseException("association.not.found", "ASSOC_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        Tontine tontine = TontineMapper.toEntity(request, association);
        Tontine saved = tontineRepository.save(tontine);

        auditLogService.logAction("CREATE_TONTINE", "Tontine", saved.getId(), admin);

        return enrichResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TontineResponse getTontineById(Long id) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        // Sécurité : On vérifie que la tontine appartient bien à l'association de l'utilisateur
        Tontine tontine = tontineRepository.findByIdAndAssociationId(id, associationId)
                .orElseThrow(() -> new BaseException("tontine.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        return enrichResponse(tontine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TontineResponse> getAssociationTontines(boolean onlyActive) {
        // Plus besoin de passer associationId en paramètre de méthode !
        Long associationId = SecurityUtils.getCurrentAssociationId();

        List<Tontine> tontines = onlyActive
                ? tontineRepository.findByAssociationIdAndActiveTrue(associationId)
                : tontineRepository.findByAssociationId(associationId);

        return tontines.stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    @Transactional
    public TontineResponse updateTontineStatus(Long id, boolean active) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        Tontine tontine = tontineRepository.findByIdAndAssociationId(id, associationId)
                .orElseThrow(() -> new BaseException("tontine.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        tontine.setActive(active);
        Tontine updated = tontineRepository.save(tontine);

        auditLogService.logAction(active ? "ACTIVATE_TONTINE" : "DEACTIVATE_TONTINE", "Tontine", id, admin);

        return enrichResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTontine(Long id) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        Tontine tontine = tontineRepository.findByIdAndAssociationId(id, associationId)
                .orElseThrow(() -> new BaseException("tontine.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        // Intégrité métier
        long memberCount = memberTontineRepository.countByTontineId(id);
        if (memberCount > 0) {
            throw new BaseException("tontine.not.empty", "FORBIDDEN_DELETE", HttpStatus.BAD_REQUEST);
        }

        tontineRepository.delete(tontine);
        auditLogService.logAction("DELETE_TONTINE", "Tontine", id, admin);
    }


    /**
     * Utilise les repositories annexes pour peupler les champs statistiques du DTO
     */
    private TontineResponse enrichResponse(Tontine tontine) {
        Long totalMembers = memberTontineRepository.countByTontineId(tontine.getId());
        // On s'assure que sumPaidAmountByTontineId gère les retours null (BigDecimal.ZERO)
        BigDecimal totalCollected = contributionRepository.sumPaidAmountByTontineId(tontine.getId());
        if(totalCollected == null) totalCollected = BigDecimal.ZERO;

        return TontineMapper.toResponse(tontine, totalMembers, totalCollected);
    }

}
