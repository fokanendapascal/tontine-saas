package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.AssociationRequest;
import com.techsolution.tontine_saas.dtos.response.AssociationResponse;
import com.techsolution.tontine_saas.services.AssociationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/associations")
@RequiredArgsConstructor
@Tag(name = "Associations", description = "API de gestion des organisations (Tenants)")
public class AssociationController {

    private final AssociationService associationService;

    @Operation(summary = "Enregistrer une nouvelle association",
            description = "Crée une entité racine. Vérifie l'unicité du nom sans tenir compte de la casse.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Association créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Nom déjà utilisé ou données invalides")
    })
    @PostMapping
    public ResponseEntity<AssociationResponse> createAssociation(@RequestBody AssociationRequest request) {
        AssociationResponse response = associationService.createAssociation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Mettre à jour les informations d'une association")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssociationResponse> updateAssociation(
            @PathVariable Long id,
            @RequestBody AssociationRequest request
    ) {
        AssociationResponse updatedAssociation = associationService.updateAssociation(id, request);
        return ResponseEntity.ok(updatedAssociation);
    }

    @Operation(summary = "Obtenir les détails et statistiques d'une association")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssociationResponse> getAssociationById(@PathVariable Long id) {
        AssociationResponse association = associationService.getAssociationById(id);
        return ResponseEntity.ok(association);
    }

    @Operation(summary = "Lister toutes les associations du système")
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AssociationResponse>> getAllAssociations() {
        List<AssociationResponse> associations = associationService.getAllAssociations();
        return ResponseEntity.ok(associations);
    }

    @Operation(summary = "Désactiver une association (Soft Delete)")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateAssociation(@PathVariable Long id) {
        associationService.deactivateAssociation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Réactiver une association",
            description = "Restaure le statut actif d'une association précédemment désactivée.")
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> activateAssociation(@PathVariable Long id) {
        associationService.activateAssociation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Supprimer définitivement une association",
            description = "Action impossible si l'association possède encore des membres rattachés.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAssociation(@PathVariable Long id) {
        associationService.deleteAssociation(id);
        return ResponseEntity.noContent().build();
    }

}
