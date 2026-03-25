package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API de gestion des membres et utilisateurs de la plateforme")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    //Build add User API REST
    @Operation(summary = "Créer un nouvel utilisateur",
            description = "L'association de l'utilisateur est automatiquement celle de l'admin créateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request){
        // Ajoutez ce log pour voir ce que Spring reçoit réellement
        System.out.println("Request received: " + request.getFirstName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    //Build get User API REST
    @Operation(summary = "Récupérer un utilisateur par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long userId){
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Récupérer un utilisateur par son email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable("email") String email){
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    //Build get all Users by association API REST
    @Operation(summary = "Lister tous les membres de l'association connectée")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        // L'associationId est récupéré silencieusement dans le service via le token
        List<UserResponse> users = userService.getUsersByAssociation();
        return ResponseEntity.ok(users);
    }

    //Build update User status REST API
    @Operation(summary = "Activer ou désactiver un compte utilisateur")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable("id") Long id,
            @RequestParam boolean active
    ){
        UserResponse userResponse = userService.updateUserStatus(id, active);
        return ResponseEntity.ok(userResponse);
    }

    //Build delete User REST API
    @Operation(summary = "Supprimer définitivement un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
