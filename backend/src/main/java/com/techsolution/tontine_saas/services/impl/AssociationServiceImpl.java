package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.AssociationRequest;
import com.techsolution.tontine_saas.dtos.response.AssociationResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.exceptions.BusinessException;
import com.techsolution.tontine_saas.exceptions.ValidationException;
import com.techsolution.tontine_saas.mappers.AssociationMapper;
import com.techsolution.tontine_saas.repository.AssociationRepository;
import com.techsolution.tontine_saas.repository.TontineRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.AssociationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssociationServiceImpl implements AssociationService {

    private final AssociationRepository associationRepository;
    private final UserRepository userRepository;
    private final TontineRepository tontineRepository;

    // 🔥 CREATE
    @Override
    @Transactional
    public AssociationResponse createAssociation(AssociationRequest request) {
        if (associationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BaseException("association.exists", "ASSOCIATION_ALREADY_EXISTS", HttpStatus.CONFLICT);
        }

        Association association = AssociationMapper.toEntity(request);
        // Initialisation par défaut pour un SaaS
        association.setActive(true);

        Association saved = associationRepository.save(association);
        return buildResponseWithStats(saved);
    }

    // 🔥 UPDATE
    @Override
    @Transactional
    public AssociationResponse updateAssociation(Long id, AssociationRequest request) {
        // Sécurité : Vérifier que l'admin modifie bien sa propre association
        checkAccess(id);

        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BaseException("association.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!association.getName().equalsIgnoreCase(request.getName())
                && associationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BaseException("association.exists", "NAME_TAKEN", HttpStatus.CONFLICT);
        }

        association.setName(request.getName());
        association.setCountry(request.getCountry());
        association.setCurrency(request.getCurrency());
        if (request.getActive() != null) {
            association.setActive(request.getActive());
        }

        return buildResponseWithStats(associationRepository.save(association));
    }

    // 🔥 GET BY ID
    @Override
    @Transactional(readOnly = true)
    public AssociationResponse getAssociationById(Long id) {
        // Sécurité Multi-tenant
        checkAccess(id);

        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BaseException("association.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        return buildResponseWithStats(association);
    }

    // 🔥 GET ALL
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<AssociationResponse> getAllAssociations() {
        // Note : Dans un vrai SaaS, un Admin d'association ne devrait pas "voir" les autres.
        // Cette méthode est généralement réservée à un "Super Admin" plateforme.
        return associationRepository.findAll()
                .stream()
                .map(this::buildResponseWithStats)
                .toList();
    }

    // 🔥 SOFT DELETE (Deactivate)
    @Override
    @Transactional
    public void deactivateAssociation(Long id) {
        checkAccess(id);
        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BaseException("association.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));
        association.setActive(false);
        associationRepository.save(association);
    }

    // 🔥 REACTIVATE
    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')") // Seul le Super Admin peut réactiver une asso
    public AssociationResponse activateAssociation(Long id) {
        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BaseException("association.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (Boolean.TRUE.equals(association.getActive())) {
            throw new BaseException("association.already.active", "CONFLICT", HttpStatus.CONFLICT);
        }

        association.setActive(true);
        return buildResponseWithStats(associationRepository.save(association));
    }

    // 🔥 HARD DELETE
    @Override
    @Transactional
    public void deleteAssociation(Long id) {
        checkAccess(id);
        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BaseException("association.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (userRepository.countByAssociationId(id) > 0) {
            throw new BaseException("association.has.users", "CANNOT_DELETE_ACTIVE_DATA", HttpStatus.BAD_REQUEST);
        }

        associationRepository.delete(association);
    }

    /**
     * Optimisation : Centralisation des stats
     */
    private AssociationResponse buildResponseWithStats(Association association) {
        Long id = association.getId();
        return AssociationMapper.toResponse(
                association,
                userRepository.countByAssociationId(id),
                tontineRepository.countByAssociationId(id),
                userRepository.countByAssociationIdAndActiveTrue(id)
        );
    }

    /**
     * Vérifie si l'utilisateur connecté a le droit d'accéder à cette association.
     */
    private void checkAccess(Long associationId) {
        Long currentAssocId = SecurityUtils.getCurrentAssociationId();
        // Si l'utilisateur n'est pas SuperAdmin et essaie d'accéder à une autre asso
        if (!currentAssocId.equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_TENANT_ACCESS", HttpStatus.FORBIDDEN);
        }
    }

}