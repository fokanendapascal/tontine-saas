package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.TokenRefreshRequest;
import com.techsolution.tontine_saas.dtos.request.UserLoginRequest;
import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.AuthResponse;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur responsable de la sécurité et de l'accès.
 * Gère l'enregistrement des nouveaux utilisateurs et la génération de jetons d'accès.
 */
@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentications", description = "Api de gestion des authentifications(Inscription, Connexion et récupération du profil)")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Inscription d'un nouvel utilisateur",
            description = "Crée un nouveau compte utilisateur dans le système et retourne un jeton d'authentification initial."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou email déjà utilisé"),
            @ApiResponse(responseCode = "422", description = "Erreur de validation des champs(ex: format email, mot de passe trop court)")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRequest userRequest) {
        log.info("Inscription d'un nouvel utilisateur : {}", userRequest.getEmail());
        return new ResponseEntity<>(authService.register(userRequest), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Connexion utilisateur",
            description = "Authentifie l'utilisateur via ses identifiants (email/password) et retourne un JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification réussie",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
            @ApiResponse(responseCode = "403", description = "Compte désactivé ou banni")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);

        log.info("Connexion réussie pour {}", loginRequest.getEmail());
        return ResponseEntity.ok(response);

    }

    @Operation(
            summary = "Récupérer l'utilisateur authentifié",
            description = "Retourne les informations détaillées de l'utilisateur correspondant au jeton JWT envoyé dans les headers."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil utilisateur récupéré"),
            @ApiResponse(responseCode = "401", description = "Jeton invalide ou expiré"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur lors de la récupération du profil")
    })
    @GetMapping("/authenticated")
    public ResponseEntity<UserResponse> getAuth() {
        return ResponseEntity.ok(authService.getAuthenticatedUserResponse());
    }

    /**
     * Correction : Utilisation d'un DTO dédié pour le refresh
     * plutôt que de détourner le header Authorization.
     */
    @Operation(summary = "Renouveler le jeton d'accès")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @Operation(summary = "Déconnexion de l'utilisateur")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Logique de blacklistage du token si nécessaire
        authService.logout();
        return ResponseEntity.noContent().build();
    }

}
