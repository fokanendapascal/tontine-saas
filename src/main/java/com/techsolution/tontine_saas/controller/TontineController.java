package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.TontineRequest;
import com.techsolution.tontine_saas.dtos.response.TontineResponse;
import com.techsolution.tontine_saas.services.TontineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/tontines")
@Tag(name = "Tontines" , description = "Api de gestion des tontines")
public class TontineController {

    private final TontineService tontineService;

    //Build add tontine API REST
    @Operation(summary = "Créer une nouvelle tontine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tontine créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping("/admin/{adminId}")
    public ResponseEntity<TontineResponse> createTontine(
            @RequestBody TontineRequest tontineRequest,
            @PathVariable Long adminId
    ){
        TontineResponse savedTontine = tontineService.createTontine(tontineRequest, adminId);
        return new ResponseEntity<>(savedTontine, HttpStatus.CREATED);
    }

    //Build get Tontine API REST
    @Operation(summary = "Récupérer une tontine par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tontine trouvée"),
            @ApiResponse(responseCode = "404", description = "Tontine non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TontineResponse> getTontineById(
            @Parameter(description = "Identifiant unique de la tontine") @PathVariable("id") Long tontineId
    ){
        TontineResponse tontineResponse = tontineService.getTontineById(tontineId);
        return ResponseEntity.ok(tontineResponse);
    }

    //Build get all tontines API REST
    @Operation(summary = "Lister toutes les tontines par association")
    @GetMapping("/association/{id}")
    public ResponseEntity<List<TontineResponse>> getAssociationTontines(
            @PathVariable("id") Long associationId,
            @RequestParam(defaultValue = "false") boolean onlyActive
    ){
        List<TontineResponse> tontines = tontineService.getAssociationTontines(associationId, onlyActive);
        return ResponseEntity.ok(tontines);
    }

    //Build update tontine status API REST
    @Operation(summary = "Mettre à jour le statut d'une tontine")
    @PutMapping("/{id}/status/{idAdmin}")
    public ResponseEntity<TontineResponse> updateTontineStatus(
            @PathVariable("id") Long id,
            @RequestParam boolean active,
            @PathVariable("idAdmin") Long adminId
    ){
        TontineResponse tontineResponse = tontineService.updateTontineStatus(id, active, adminId);
        return ResponseEntity.ok(tontineResponse);
    }

    //Build delete User REST API
    @Operation(summary = "Supprimer une tontine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tontine supprimée"),
            @ApiResponse(responseCode = "404", description = "Tontine non trouvée")
    })
    @DeleteMapping("/{id}/{idAdmin}") // Séparation par des slashs
    public ResponseEntity<Void> deleteTontine(
            @PathVariable("id") Long tontineId,
            @PathVariable("idAdmin") Long adminId
    ) {
        tontineService.deleteTontine(tontineId, adminId);
        return ResponseEntity.noContent().build();
    }
}
