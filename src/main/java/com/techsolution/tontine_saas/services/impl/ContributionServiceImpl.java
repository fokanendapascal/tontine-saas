package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.ContributionRequest;
import com.techsolution.tontine_saas.dtos.response.ContributionResponse;
import com.techsolution.tontine_saas.dtos.response.LateMemberResponse;
import com.techsolution.tontine_saas.entities.*;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.ContributionMapper;
import com.techsolution.tontine_saas.repository.ContributionRepository;
import com.techsolution.tontine_saas.repository.MemberTontineRepository;
import com.techsolution.tontine_saas.repository.TontineRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.ContributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;
    private final MemberTontineRepository memberTontineRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final TontineRepository tontineRepository;

    @Override
    @Transactional
    public ContributionResponse payContribution(ContributionRequest request) {
        // 1. Identification via le SecurityContext
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        // 2. Récupérer le membre et vérifier son appartenance à l'association
        MemberTontine member = memberTontineRepository.findById(request.getMemberTontineId())
                .orElseThrow(() -> new BaseException("member.not.found", "MEMBER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!member.getTontine().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_ASSOCIATION", HttpStatus.FORBIDDEN);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("user.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 3. Logique de calcul automatique de pénalité
        BigDecimal penalty = request.getPenalty() != null ? request.getPenalty() : BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getAutoCalculatePenalty()) && request.getPaymentDate() != null) {
            if (request.getPaymentDate().isAfter(request.getDueDate())) {
                // Règle métier : 5% de pénalité si retard détecté
                penalty = request.getAmount().multiply(new BigDecimal("0.05"));
            }
        }

        // 4. Conversion et sauvegarde
        Contribution contribution = ContributionMapper.toEntity(request, member);
        contribution.setPenalty(penalty);
        contribution.setStatus(ContributionStatus.PAID); // Statut forcé au paiement

        Contribution saved = contributionRepository.save(contribution);

        // 5. Audit
        auditLogService.logAction("PAY_CONTRIBUTION", "Contribution", saved.getId(), admin);

        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContributionResponse> getMemberHistory(Long memberTontineId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        // Vérification de sécurité
        MemberTontine member = memberTontineRepository.findById(memberTontineId)
                .orElseThrow(() -> new BaseException("member.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!member.getTontine().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "UNAUTHORIZED", HttpStatus.FORBIDDEN);
        }

        return contributionRepository.findByMemberTontineIdOrderByDueDateDesc(memberTontineId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContributionResponse> getTontineHistory(Long tontineId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        // Vérification que la tontine appartient à l'asso connectée
        if (!tontineRepository.existsByIdAndAssociationId(tontineId, associationId)) {
            throw new BaseException("tontine.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        return contributionRepository.findByMemberTontine_Tontine_Id(tontineId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContributionResponse getContributionById(Long id) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new BaseException("contribution.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));
        return convertToResponse(contribution);
    }

    @Override
    @Transactional
    public void updateContributionStatus(Long id, String status) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new BaseException("contribution.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!contribution.getMemberTontine().getTontine().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("user.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        contribution.setStatus(ContributionStatus.valueOf(status));
        contributionRepository.save(contribution);

        auditLogService.logAction("UPDATE_STATUS", "Contribution", id, admin);
    }


    /**
     * Helper pour enrichir la réponse avec le total payé par le membre
     */
    @Override
    @Transactional(readOnly = true)
    public List<LateMemberResponse> getLateMembers(Long tontineId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        // Sécurité Multi-tenant
        if (!tontineRepository.existsByIdAndAssociationId(tontineId, associationId)) {
            throw new BaseException("tontine.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        List<Contribution> lateContributions = contributionRepository
                .findByMemberTontineTontineIdAndStatus(tontineId, ContributionStatus.LATE);

        return lateContributions.stream()
                .collect(Collectors.groupingBy(c -> c.getMemberTontine().getUser()))
                .entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<Contribution> memberLates = entry.getValue();

                    BigDecimal totalDebt = memberLates.stream()
                            .map(Contribution::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return LateMemberResponse.builder()
                            .userId(user.getId())
                            .fullName(user.getFirstName() + " " + user.getLastName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .numberOfLateContributions(memberLates.size())
                            .totalDebt(totalDebt)
                            .lastDueDate(memberLates.isEmpty() ? null : memberLates.get(0).getDueDate())
                            .build();
                })
                .toList();
    }

    private ContributionResponse convertToResponse(Contribution contribution) {
        BigDecimal totalPaid = contributionRepository.sumByMemberTontineId(contribution.getMemberTontine().getId());
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;
        return ContributionMapper.toResponse(contribution, totalPaid);
    }

}
