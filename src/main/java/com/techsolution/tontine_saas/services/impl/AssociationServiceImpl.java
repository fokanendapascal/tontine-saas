package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.AssociationRequest;
import com.techsolution.tontine_saas.dtos.response.AssociationResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.exceptions.BusinessException;
import com.techsolution.tontine_saas.exceptions.ValidationException;
import com.techsolution.tontine_saas.mappers.AssociationMapper;
import com.techsolution.tontine_saas.repository.AssociationRepository;
import com.techsolution.tontine_saas.repository.TontineRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.services.AssociationService;
import lombok.RequiredArgsConstructor;
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
    public AssociationResponse createAssociation(AssociationRequest request) {

        if (associationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ValidationException("association.already.exists");
        }

        Association association = AssociationMapper.toEntity(request);
        association = associationRepository.save(association);

        return buildResponseWithStats(association);
    }

    // 🔥 UPDATE
    @Override
    public AssociationResponse updateAssociation(Long id, AssociationRequest request) {

        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("association.not.found"));

        if (!association.getName().equals(request.getName())
                && associationRepository.existsByNameIgnoreCase(request.getName())) {

            throw new ValidationException("association.already.exists");
        }

        association.setName(request.getName());
        association.setCountry(request.getCountry());
        association.setCurrency(request.getCurrency());
        association.setActive(
                request.getActive() != null
                        ? request.getActive()
                        : association.getActive()
        );

        association = associationRepository.save(association);

        return buildResponseWithStats(association);
    }

    // 🔥 GET BY ID
    @Override
    @Transactional(readOnly = true)
    public AssociationResponse getAssociationById(Long id) {

        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("association.not.found"));

        return buildResponseWithStats(association);
    }

    // 🔥 GET ALL
    @Override
    @Transactional(readOnly = true)
    public List<AssociationResponse> getAllAssociations() {

        return associationRepository.findAll()
                .stream()
                .map(this::buildResponseWithStats)
                .toList();
    }

    // 🔥 SOFT DELETE (Deactivate)
    @Override
    public void deactivateAssociation(Long id) {

        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("association.not.found"));

        association.setActive(false);

        associationRepository.save(association);
    }

    // 🔥 HARD DELETE
    @Override
    public void deleteAssociation(Long id) {

        Association association = associationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("association.not.found"));

        if (userRepository.countByAssociationId(id) > 0) {
            throw new BusinessException("association.has.users");
        }

        associationRepository.delete(association);
    }

    // 🔥 Méthode privée centralisée pour éviter duplication
    private AssociationResponse buildResponseWithStats(Association association) {

        Long associationId = association.getId();

        Long numberOfUsers = userRepository.countByAssociationId(associationId);
        Long numberOfTontines = tontineRepository.countByAssociationId(associationId);
        Long activeUsers = userRepository.countByAssociationIdAndActiveTrue(associationId);

        return AssociationMapper.toResponse(
                association,
                numberOfUsers,
                numberOfTontines,
                activeUsers
        );
    }
}