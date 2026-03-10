package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.UserLoginRequest;
import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.AuthResponse;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.Role;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.UserMapper;
import com.techsolution.tontine_saas.repository.AssociationRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.JwtUtil;
import com.techsolution.tontine_saas.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AssociationRepository associationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(UserLoginRequest userLoginRequest) {
        // 1. Authentification Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequest.getEmail(),
                        userLoginRequest.getPassword()
                )
        );

        // 2. Récupération utilisateur (avec ses relations pour le mapper)
        User user = userRepository.findByEmailWithAssociation(userLoginRequest.getEmail())
                .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse register(UserRequest userRequest) {
        // 1. Vérification email
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BaseException("user.exists", "USER_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
        }

        // 2. Récupération de l'association (indispensable pour éviter l'ID null)
        Association association = associationRepository.findById(userRequest.getAssociationId())
                .orElseThrow(() -> new BaseException("association.not.found", "ASSOC_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 3. Utilisation du Mapper pour créer l'entité
        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        User newUser = UserMapper.toEntity(userRequest, association, encodedPassword);

        // Sécurité par défaut sur les rôles si vide
        if (newUser.getRoles() == null || newUser.getRoles().isEmpty()) {
            newUser.setRoles(List.of(Role.MEMBER));
        }

        User savedUser = userRepository.save(newUser);
        return generateAuthResponse(savedUser);
    }

    @Override
    public User getAuthenticatedUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AccessDeniedException("User not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found" + authentication.getName()));
    }

    @Override
    public UserResponse getAuthenticatedUserResponse() {
        // On renvoie des valeurs par défaut pour les stats dans le contexte auth simple
        return UserMapper.toResponse(getAuthenticatedUserEntity(), 0, false);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken != null && jwtUtil.isTokenValid(refreshToken)) {
            String email = jwtUtil.extractEmail(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND));
            return generateAuthResponse(user);
        }
        throw new AccessDeniedException("Invalid Refresh Token");
    }

    /**
     * 🔥 Centralisation de la génération des jetons avec Association ID
     */
    private AuthResponse generateAuthResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.name())
                .collect(Collectors.toList());

        // Récupération sécurisée de l'ID de l'association
        Long associationId = (user.getAssociation() != null) ? user.getAssociation().getId() : null;

        // 🔥 Appel de la nouvelle signature JwtUtil avec associationId
        String accessToken = jwtUtil.generateToken(user.getEmail(), roles, associationId);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Valeurs par défaut pour les stats (à enrichir plus tard)
        Integer tontines = 0;
        Boolean isEligible = false;

        return new AuthResponse(
                accessToken,
                refreshToken,
                UserMapper.toResponse(user, tontines, isEligible)
        );
    }

}
