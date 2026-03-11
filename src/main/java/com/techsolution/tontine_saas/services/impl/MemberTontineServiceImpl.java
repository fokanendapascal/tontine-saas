package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.MemberTontineRequest;
import com.techsolution.tontine_saas.dtos.response.MemberTontineResponse;
import com.techsolution.tontine_saas.entities.ContributionStatus;
import com.techsolution.tontine_saas.entities.MemberTontine;
import com.techsolution.tontine_saas.entities.Tontine;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.MemberTontineMapper;
import com.techsolution.tontine_saas.repository.ContributionRepository;
import com.techsolution.tontine_saas.repository.MemberTontineRepository;
import com.techsolution.tontine_saas.repository.TontineRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.MemberTontineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberTontineServiceImpl implements MemberTontineService {

    private final MemberTontineRepository memberTontineRepository;
    private final UserRepository userRepository;
    private final TontineRepository tontineRepository;
    private final ContributionRepository contributionRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public MemberTontineResponse addMemberToTontine(MemberTontineRequest request) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        // 1. Vérifier si le membre est déjà dans cette tontine
        if (memberTontineRepository.existsByUserIdAndTontineId(request.getUserId(), request.getTontineId())) {
            throw new BaseException("member.already.exists", "DUPLICATE_MEMBER", HttpStatus.BAD_REQUEST);
        }

        // 2. Récupérer et vérifier l'appartenance à l'association (Sécurité Multi-tenant)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!user.getAssociation().getId().equals(associationId)) {
            throw new BaseException("user.not.in.association", "FORBIDDEN_ACCESS", HttpStatus.FORBIDDEN);
        }

        Tontine tontine = tontineRepository.findByIdAndAssociationId(request.getTontineId(), associationId)
                .orElseThrow(() -> new BaseException("tontine.not.found", "TONTINE_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 3. Gestion de l'ordre de passage (Auto-assignation)
        if (Boolean.TRUE.equals(request.getAutoAssignOrder())) {
            int currentCount = (int) memberTontineRepository.countByTontineId(tontine.getId());
            request.setOrderPosition(currentCount + 1);
        }

        // 4. Mapping et Sauvegarde
        MemberTontine memberTontine = MemberTontineMapper.toEntity(request, user, tontine);
        MemberTontine saved = memberTontineRepository.save(memberTontine);

        // 5. Audit
        auditLogService.logAction("ADD_MEMBER_TO_TONTINE", "MemberTontine", saved.getId(), admin);

        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberTontineResponse> getMembersByTontine(Long tontineId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        // Vérifier que la tontine appartient à l'association avant de lister les membres
        if (!tontineRepository.existsByIdAndAssociationId(tontineId, associationId)) {
            throw new BaseException("tontine.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        return memberTontineRepository.findByTontineId(tontineId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberTontineResponse> getTontinesByUser(Long userId) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        // Un utilisateur ne peut voir que ses propres tontines ou celles de son asso (si admin)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException("user.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!user.getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        return memberTontineRepository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberTontineResponse getMemberDetails(Long userId, Long tontineId) {
        MemberTontine member = memberTontineRepository.findByUserIdAndTontineId(userId, tontineId)
                .orElseThrow(() -> new BaseException("member.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));
        return convertToResponse(member);
    }

    @Override
    @Transactional
    public void removeMemberFromTontine(Long id) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        MemberTontine member = memberTontineRepository.findById(id)
                .orElseThrow(() -> new BaseException("member.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        // Vérifier que la tontine rattachée appartient à l'asso
        if (!member.getTontine().getAssociation().getId().equals(associationId)) {
            throw new BaseException("access.denied", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Intégrité financière : Interdire la suppression si des contributions existent
        if (contributionRepository.countByMemberTontine_Id(id) > 0) {
            throw new BaseException("member.has.contributions", "FORBIDDEN_DELETE", HttpStatus.BAD_REQUEST);
        }

        memberTontineRepository.delete(member);
        auditLogService.logAction("REMOVE_MEMBER_FROM_TONTINE", "MemberTontine", id, admin);
    }

    /**
     * Utilise le mapper pour transformer l'entité en réponse enrichie
     */
    private MemberTontineResponse convertToResponse(MemberTontine entity) {

        long contributionCount = contributionRepository
                .countByMemberTontine_Id(entity.getId());

        BigDecimal totalPaid = Optional.ofNullable(
                contributionRepository.sumByMemberTontineId(
                        entity.getId(),
                        ContributionStatus.PAID
                )
        ).orElse(BigDecimal.ZERO);

        return MemberTontineMapper.toResponse(entity, contributionCount, totalPaid);
    }

}
