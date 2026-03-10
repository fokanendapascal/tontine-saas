package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.ContributionRequest;
import com.techsolution.tontine_saas.dtos.response.ContributionResponse;
import com.techsolution.tontine_saas.dtos.response.LateMemberResponse;
import com.techsolution.tontine_saas.services.ContributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/contributions")
@RequiredArgsConstructor
@Tag(name = "Contributions", description = "API de gestion des cotisations, paiements et pénalités")
public class ContributionController {

    private final ContributionService contributionService;

    @Operation(
            summary = "Enregistrer le paiement d'une cotisation",
            description = "L'admin et l'association sont identifiés par le token JWT. Calcule auto 5% de pénalité si retard.")
    @PostMapping("/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContributionResponse> payContribution(
            @RequestBody ContributionRequest request
    ) {
        // Le service gère l'ID admin et l'ID association via SecurityContext
        ContributionResponse response = contributionService.payContribution(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Historique des cotisations d'un membre spécifique")
    @GetMapping("/member/{memberTontineId}")
    public ResponseEntity<List<ContributionResponse>> getMemberHistory(
            @PathVariable Long memberTontineId
    ) {
        List<ContributionResponse> history = contributionService.getMemberHistory(memberTontineId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Historique global des cotisations d'une tontine")
    @GetMapping("/tontine/{tontineId}")
    public ResponseEntity<List<ContributionResponse>> getTontineHistory(
            @PathVariable Long tontineId
    ) {
        List<ContributionResponse> history = contributionService.getTontineHistory(tontineId);
        return ResponseEntity.ok(history);
    }

    @Operation(
            summary = "Liste des membres en retard",
            description = "Retourne les membres ayant des cotisations 'LATE' avec calcul de la dette totale.")
    @GetMapping("/tontine/{tontineId}/late-members")
    public ResponseEntity<List<LateMemberResponse>> getLateMembers(
            @PathVariable Long tontineId
    ) {
        List<LateMemberResponse> lates = contributionService.getLateMembers(tontineId);
        return ResponseEntity.ok(lates);
    }

    @Operation(summary = "Forcer la mise à jour du statut d'une cotisation")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        // Passage du statut par RequestParam pour plus de flexibilité (ex: ?status=PAID)
        contributionService.updateContributionStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer les détails d'une cotisation par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<ContributionResponse> getContributionById(@PathVariable Long id) {
        ContributionResponse details = contributionService.getContributionById(id);
        return ResponseEntity.ok(details);
    }

}
