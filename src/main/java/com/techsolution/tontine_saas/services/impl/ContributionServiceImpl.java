package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.ContributionRequest;
import com.techsolution.tontine_saas.dtos.response.ContributionResponse;
import com.techsolution.tontine_saas.dtos.response.LateMemberResponse;
import com.techsolution.tontine_saas.entities.*;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.ContributionMapper;
import com.techsolution.tontine_saas.repository.ContributionRepository;
import com.techsolution.tontine_saas.repository.MemberTontineRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
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

    @Override
    @Transactional
    public ContributionResponse payContribution(ContributionRequest request, Long adminId) {
        // 1. Récupérer le membre et l'admin
        MemberTontine member = memberTontineRepository.findById(request.getMemberTontineId())
                .orElseThrow(() -> new BaseException("member.not.found", "MEMBER_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("user.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 2. Logique de calcul automatique de pénalité (si activé)
        BigDecimal penalty = request.getPenalty() != null ? request.getPenalty() : BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getAutoCalculatePenalty()) && request.getPaymentDate() != null) {
            if (request.getPaymentDate().isAfter(request.getDueDate())) {
                // Exemple : 5% du montant par défaut (à adapter selon vos règles métier)
                penalty = request.getAmount().multiply(new BigDecimal("0.05"));
            }
        }

        // 3. Conversion et sauvegarde
        Contribution contribution = ContributionMapper.toEntity(request, member);
        contribution.setPenalty(penalty);
        Contribution saved = contributionRepository.save(contribution);

        // 4. Audit
        auditLogService.logAction("PAY_CONTRIBUTION", "Contribution", saved.getId(), admin);

        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContributionResponse> getMemberHistory(Long memberTontineId) {
        return contributionRepository.findByMemberTontineIdOrderByDueDateDesc(memberTontineId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContributionResponse> getTontineHistory(Long tontineId) {
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
    public void updateContributionStatus(Long id, String status, Long adminId) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new BaseException("contribution.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("user.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        contribution.setStatus(ContributionStatus.valueOf(status));
        contributionRepository.save(contribution);

        auditLogService.logAction("UPDATE_STATUS", "Contribution", id, admin);
    }

    /**
     * Helper pour enrichir la réponse avec le total payé par le membre
     */
    private ContributionResponse convertToResponse(Contribution contribution) {
        BigDecimal totalPaid = contributionRepository.sumByMemberTontineId(contribution.getMemberTontine().getId());
        return ContributionMapper.toResponse(contribution, totalPaid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LateMemberResponse> getLateMembers(Long tontineId) {
        // 1. Récupérer toutes les cotisations qui sont en retard (Statut LATE)
        List<Contribution> lateContributions = contributionRepository
                .findByMemberTontineTontineIdAndStatus(tontineId, ContributionStatus.LATE);

        // 2. Regrouper par membre pour envoyer une seule relance globale
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
                            .lastDueDate(memberLates.getFirst().getDueDate())
                            .build();
                })
                .collect(Collectors.toList());
    }


}
