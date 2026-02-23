package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.MemberTontineRequest;
import com.techsolution.tontine_saas.dtos.response.MemberTontineResponse;
import com.techsolution.tontine_saas.entities.MemberTontine;
import com.techsolution.tontine_saas.entities.Tontine;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.MemberTontineMapper;
import com.techsolution.tontine_saas.repository.ContributionRepository;
import com.techsolution.tontine_saas.repository.MemberTontineRepository;
import com.techsolution.tontine_saas.repository.TontineRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.MemberTontineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
    public MemberTontineResponse addMemberToTontine(MemberTontineRequest request, Long adminId) {
        // 1. Vérifier si le membre est déjà dans cette tontine
        if (memberTontineRepository.existsByUserIdAndTontineId(request.getUserId(), request.getTontineId())) {
            throw new BaseException("member.already.exists", "DUPLICATE_MEMBER", HttpStatus.BAD_REQUEST);
        }

        // 2. Récupérer les entités nécessaires
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        Tontine tontine = tontineRepository.findById(request.getTontineId())
                .orElseThrow(() -> new BaseException("tontine.not.found", "TONTINE_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 3. Gestion de l'ordre de passage (Auto-assignation si demandée)
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
        return memberTontineRepository.findByTontineId(tontineId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberTontineResponse> getTontinesByUser(Long userId) {
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
    public void removeMemberFromTontine(Long id, Long adminId) {
        MemberTontine member = memberTontineRepository.findById(id)
                .orElseThrow(() -> new BaseException("member.not.found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Vérification : un membre ayant déjà cotisé ne devrait probablement pas être supprimé brutalement
        long contributions = contributionRepository.countByMemberTontine_Tontine_Id(id);
        if (contributions > 0) {
            throw new BaseException("member.has.contributions", "FORBIDDEN_DELETE", HttpStatus.BAD_REQUEST);
        }

        memberTontineRepository.delete(member);
        auditLogService.logAction("REMOVE_MEMBER_FROM_TONTINE", "MemberTontine", id, admin);
    }

    /**
     * Utilise le mapper pour transformer l'entité en réponse enrichie
     */
    private MemberTontineResponse convertToResponse(MemberTontine entity) {
        long count = contributionRepository.countByMemberTontine_Tontine_Id(entity.getId());
        BigDecimal total = contributionRepository.sumByMemberTontineId(entity.getId());

        return MemberTontineMapper.toResponse(entity, count, total);
    }
}
