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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        // 1. Récupérer le membre et vérifier l'isolation multi-tenant
        MemberTontine member = memberTontineRepository.findById(request.getMemberTontineId())
                .orElseThrow(() -> new BaseException("member.not.found", "MEMBER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!member.getTontine().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_ASSOCIATION_ACCESS", HttpStatus.FORBIDDEN);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("user.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 2. Calcul automatique de pénalité (BigDecimal sécurisé)
        BigDecimal penalty = request.getPenalty() != null ? request.getPenalty() : BigDecimal.ZERO;

        if (Boolean.TRUE.equals(request.getAutoCalculatePenalty()) && request.getPaymentDate() != null) {
            // Si la date de paiement est après la date d'échéance
            if (request.getPaymentDate().isAfter(request.getDueDate())) {
                // Règle : 5% de pénalité. Utilisation de setScale pour la précision monétaire
                penalty = request.getAmount().multiply(new BigDecimal("0.05"))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        // 3. Mapping et Persistance
        Contribution contribution = ContributionMapper.toEntity(request, member);
        contribution.setPenalty(penalty);
        contribution.setStatus(ContributionStatus.PAID);
        contribution.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now());

        Contribution saved = contributionRepository.save(contribution);

        // 4. Audit
        auditLogService.logAction("PAY_CONTRIBUTION", "Contribution", saved.getId(), admin);

        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContributionResponse> getMemberHistory(Long memberTontineId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        MemberTontine member = memberTontineRepository.findById(memberTontineId)
                .orElseThrow(() -> new BaseException("member.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!member.getTontine().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "UNAUTHORIZED_ACCESS", HttpStatus.FORBIDDEN);
        }

        // Pré-calcul du total payé pour éviter N+1 requêtes dans convertToResponse
        BigDecimal totalPaid = Optional.ofNullable(
                contributionRepository.sumByMemberTontineId(memberTontineId, ContributionStatus.PAID)
        ).orElse(BigDecimal.ZERO);

        return contributionRepository.findByMemberTontine_IdOrderByDueDateDesc(memberTontineId)
                .stream()
                .map(c -> ContributionMapper.toResponse(c, totalPaid))
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
            throw new BaseException("access.denied", "FORBIDDEN_TENANT_ACCESS", HttpStatus.FORBIDDEN);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("user.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        try {
            contribution.setStatus(ContributionStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BaseException("invalid.status", "INVALID_CONTRIBUTION_STATUS", HttpStatus.BAD_REQUEST);
        }

        contributionRepository.save(contribution);
        auditLogService.logAction("UPDATE_CONTRIBUTION_STATUS", "Contribution", id, admin);
    }

    /**
     * Helper pour enrichir la réponse avec le total payé par le membre
     */
    @Override
    @Transactional(readOnly = true)
    public List<LateMemberResponse> getLateMembers(Long tontineId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        if (!tontineRepository.existsByIdAndAssociationId(tontineId, associationId)) {
            throw new BaseException("tontine.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        List<Contribution> lateContributions = contributionRepository
                .findByMemberTontine_Tontine_IdAndStatus(tontineId, ContributionStatus.LATE);

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
                            .lastDueDate(memberLates.isEmpty() ? null : LocalDate.from(memberLates.getFirst().getDueDate()))
                            .build();
                })
                .toList();
    }

    private ContributionResponse convertToResponse(Contribution contribution) {

        BigDecimal totalPaid = Optional.ofNullable(
                contributionRepository.sumByMemberTontineId(
                        contribution.getMemberTontine().getId(),
                        ContributionStatus.PAID
                )
        ).orElse(BigDecimal.ZERO);

        return ContributionMapper.toResponse(contribution, totalPaid);
    }
}
