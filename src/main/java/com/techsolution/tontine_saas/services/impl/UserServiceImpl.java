package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.Savings;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.UserMapper;
import com.techsolution.tontine_saas.repository.AssociationRepository;
import com.techsolution.tontine_saas.repository.MemberTontineRepository;
import com.techsolution.tontine_saas.repository.SavingsRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.services.AuditLogService;
import com.techsolution.tontine_saas.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final PasswordEncoder passwordEncoder; // Nécessite une config BCrypt

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        // 1. Vérification unicité email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BaseException("email.already.exists", "USER_EMAIL_DUPLICATE", HttpStatus.BAD_REQUEST);
        }

        // 2. Récupérer l'association
        Association association = associationRepository.findById(request.getAssociationId())
                .orElseThrow(() -> new BaseException("association.not.found", "ASSOC_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 3. Mapper et Enregistrer (Hachage du mot de passe)
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = UserMapper.toEntity(request, association, encodedPassword);
        User savedUser = userRepository.save(user);

        // 4. Initialisation automatique du compte épargne (Business Rule)
        Savings savings = Savings.builder()
                .balance(java.math.BigDecimal.ZERO)
                .user(savedUser)
                .build();
        savingsRepository.save(savings);

        // 5. Audit
        auditLogService.logAction("CREATE_USER", "User", savedUser.getId(), savedUser);

        return enrichResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));
        return enrichResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailWithAssociation(email)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));
        return enrichResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByAssociation(Long associationId) {
        return userRepository.findByAssociationId(associationId).stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, boolean active, Long adminId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BaseException("admin.not.found", "ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND));

        user.setActive(active);
        User updated = userRepository.save(user);

        auditLogService.logAction(active ? "ACTIVATE_USER" : "DEACTIVATE_USER", "User", id, admin);
        return enrichResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, Long adminId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        // Vérification : ne pas supprimer un membre actif dans une tontine
        if (memberTontineRepository.existsByUserId(id)) {
            throw new BaseException("user.has.active.tontines", "FORBIDDEN_DELETE", HttpStatus.BAD_REQUEST);
        }

        userRepository.delete(user);
    }

    /**
     * Helper pour enrichir le UserResponse avec les infos des autres modules
     */
    private UserResponse enrichResponse(User user) {
        Integer tontineCount = (int) memberTontineRepository.countByUserId(user.getId());

        // Logique d'éligibilité simplifiée : a un compte épargne avec solde > 0
        boolean isEligible = savingsRepository.findByUserId(user.getId())
                .map(s -> s.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0)
                .orElse(false);

        return UserMapper.toResponse(user, tontineCount, isEligible);
    }

}
