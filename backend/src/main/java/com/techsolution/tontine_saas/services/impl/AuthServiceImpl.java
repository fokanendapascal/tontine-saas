package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.request.UserLoginRequest;
import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.AuthResponse;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.CustomUserDetails;
import com.techsolution.tontine_saas.entities.Role;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.exceptions.BaseException;
import com.techsolution.tontine_saas.mappers.UserMapper;
import com.techsolution.tontine_saas.repository.AssociationRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.JwtUtil;
import com.techsolution.tontine_saas.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AssociationRepository associationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(UserLoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new BaseException("auth.invalid.principal", "INVALID_AUTH_PRINCIPAL", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findFullUserByEmail(userDetails.getEmail())
                .orElseThrow(() ->
                        new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse register(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BaseException("user.exists", "USER_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
        }

        Association association = associationRepository
                .findById(userRequest.getAssociationId())
                .orElseThrow(() ->
                        new BaseException("association.not.found", "ASSOC_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());

        User newUser = UserMapper.toEntity(userRequest, association, encodedPassword);

        if (newUser.getRoles() == null || newUser.getRoles().isEmpty()) {
            newUser.setRoles(Collections.singletonList(Role.MEMBER));
        }

        return generateAuthResponse(userRepository.save(newUser));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getAuthenticatedUserResponse() {
        return UserMapper.toResponse(getAuthenticatedUserEntity(), 0, false);
    }

    @Override
    public User getAuthenticatedUserEntity() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AccessDeniedException("User not authenticated");
        }

        return userRepository.findFullUserByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + authentication.getName()
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {

        if (refreshToken == null || !jwtUtil.isTokenValid(refreshToken)) {
            throw new AccessDeniedException("Invalid or expired refresh token");
        }

        String email = jwtUtil.extractEmail(refreshToken);

        User user = userRepository.findFullUserByEmail(email)
                .orElseThrow(() ->
                        new BaseException("user.not.found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND)
                );

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BaseException("user.inactive", "USER_INACTIVE", HttpStatus.FORBIDDEN);
        }

        return generateAuthResponse(user);
    }


    @Override
    public void logout() {
        // En mode Stateless JWT, le logout est principalement géré côté Frontend
        // (suppression du token). Optionnellement, on peut blacklister le token ici.
    }

    /**
     * Centralisation de la génération des jetons avec Association ID et Rôles préfixés
     */
    private AuthResponse generateAuthResponse(User user) {

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.name().startsWith("ROLE_")
                        ? role.name()
                        : "ROLE_" + role.name())
                .collect(Collectors.toList());

        Long associationId =
                user.getAssociation() != null
                        ? user.getAssociation().getId()
                        : null;

        String accessToken =
                jwtUtil.generateToken(user.getEmail(), roles, associationId);

        String refreshToken =
                jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userResponse(UserMapper.toResponse(user, 0, false))
                .build();
    }

}
