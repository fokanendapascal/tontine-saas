package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users" , description = "Api de gestion des utilisateurs")
public class UserController {

    private UserService userService;

    //Build add User API REST
    @Operation(summary = "Créer un nouvel utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest){
        UserResponse savedUser = userService.createUser(userRequest);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    //Build get User API REST
    @Operation(summary = "Récupérer un utilisateur par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "Identifiant unique de l'utilisateur") @PathVariable("id") Long userId
    ){
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Récupérer un utilisateur par son email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
          @PathVariable("email") String email
    ){
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    //Build get all Users by association API REST
    @Operation(summary = "Lister tous les utilisateurs par association")
    @GetMapping("/association/{id}")
    public ResponseEntity<List<UserResponse>> getAllUsers(
           @Parameter(description = "Identifiant unique de l'association") @PathVariable("id") Long associationId
    ){
        List<UserResponse> users = userService.getUsersByAssociation(associationId);
        return ResponseEntity.ok(users);
    }

    //Build update User status REST API
    @Operation(summary = "Mettre à jour le statut d'un utilisateur")
    @PutMapping("/{id}/status/{idAdmin}") // Séparation par des slashs
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable("id") Long id,
            @RequestParam boolean active, // Utilisation de @RequestParam pour le booléen
            @PathVariable("idAdmin") Long idAdmin
    ){
        UserResponse userResponse = userService.updateUserStatus(id, active, idAdmin );
        return ResponseEntity.ok(userResponse);
    }

    //Build delete User REST API
    @Operation(summary = "Supprimer un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @DeleteMapping("/{id}/{idAdmin}") // Séparation par des slashs
    public ResponseEntity<Void> deleteUser(
            @PathVariable("id") Long userId,
            @PathVariable("idAdmin") Long adminId
    ) {
        userService.deleteUser(userId, adminId);
        return ResponseEntity.noContent().build();
    }
}
