package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.Savings;
import com.techsolution.tontine_saas.entities.TontineStatus;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.UserMapper;
import com.techsolution.tontine_saas.repository.AssociationRepository;
import com.techsolution.tontine_saas.repository.MemberTontineRepository;
import com.techsolution.tontine_saas.repository.SavingsRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AssociationRepository associationRepository;
    private final MemberTontineRepository memberTontineRepository;
    private final SavingsRepository savingsRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        // 1. Récupérer l'ID de l'association depuis le contexte de sécurité
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        // 2. Vérification unicité email
        if (userRepository.existsByEmailAndAssociationId(request.getEmail(), associationId)) {
            throw new BaseException("email.already.exists", "USER_EMAIL_DUPLICATE", HttpStatus.BAD_REQUEST);
        }

        // 3. Récupérer l'association du contexte (plus sûr que request.getAssociationId())
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new BaseException("association.not.found", "ASSOC_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Utilisation de getReferenceById (Lazy proxy) pour l'audit
        User admin = userRepository.getReferenceById(adminId);

        // 4. Mapper et Enregistrer
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = UserMapper.toEntity(request, association, encodedPassword);
        User savedUser = userRepository.save(user);

        // 5. Initialisation automatique du compte épargne
        Savings savings = Savings.builder()
                .balance(java.math.BigDecimal.ZERO)
                .user(savedUser)
                .build();
        savingsRepository.save(savings);

        // 6. Audit (L'admin courant est l'auteur)
        auditLogService.logAction("CREATE_USER", "User", savedUser.getId(), admin);

        return enrichResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Sécurité : Vérifier que l'utilisateur appartient à l'asso de l'appelant
        validateAssociationAccess(user, associationId);

        return enrichResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        Long associationId = SecurityUtils.getCurrentAssociationId();

        User user = userRepository.findFullUserByEmail(email)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        validateAssociationAccess(user, associationId);

        return enrichResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByAssociation() {
        // L'ID est récupéré du token, plus besoin de le passer en paramètre
        Long associationId = SecurityUtils.getCurrentAssociationId();

        return userRepository.findByAssociationId(associationId).stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, boolean active) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        validateAssociationAccess(user, associationId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        user.setActive(active);
        User updated = userRepository.save(user);

        auditLogService.logAction(active ? "ACTIVATE_USER" : "DEACTIVATE_USER", "User", id, admin);
        return enrichResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        Long associationId = SecurityUtils.getCurrentAssociationId();
        Long adminId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        validateAssociationAccess(user, associationId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Règle métier : ne pas supprimer un membre actif dans une tontine
        if (memberTontineRepository.existsByUserIdAndStatus(id, TontineStatus.ACTIVE)) {
            throw new BaseException("user.has.active.tontines", "FORBIDDEN_DELETE", HttpStatus.BAD_REQUEST);
        }

        userRepository.delete(user);
        auditLogService.logAction("DELETE_USER", "User", id, admin);
    }

    /**
     * Helper pour vérifier l'isolation des données entre associations
     *
     */
    private void validateAssociationAccess(User user, Long currentAssociationId) {
        // Si c'est un Super Admin, on ignore la restriction d'association
        if (SecurityUtils.hasRole("ROLE_SUPER_ADMIN")) {
            return;
        }

        if (!user.getAssociation().getId().equals(currentAssociationId)) {
            throw new BaseException("access.denied", "FORBIDDEN_CROSS_TENANT", HttpStatus.FORBIDDEN);
        }
    }

    private UserResponse enrichResponse(User user) {
        Integer tontineCount = (int) memberTontineRepository.countByUserId(user.getId());
        boolean isEligible = savingsRepository.findBalanceByUserId(user.getId())
                .map(balance -> balance.compareTo(BigDecimal.ZERO) > 0)
                .orElse(false);

        return UserMapper.toResponse(user, tontineCount, isEligible);
    }

}
