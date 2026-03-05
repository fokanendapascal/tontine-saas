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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/contributions")
@RequiredArgsConstructor
@Tag(name = "Contributions", description = "API de gestion des cotisations et des retards")
public class ContributionController {

    private final ContributionService contributionService;

    @Operation(
            summary = "Enregistrer le paiement d'une cotisation",
            description = "Permet de payer une cotisation. Calcule automatiquement une pénalité de 5% si le paiement est après l'échéance.")
    @PostMapping("/pay/admin/{adminId}")
    public ResponseEntity<ContributionResponse> payContribution(
            @RequestBody ContributionRequest request,
            @PathVariable Long adminId
    ) {
        ContributionResponse response = contributionService.payContribution(request, adminId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Historique des cotisations d'un membre")
    @GetMapping("/member/{memberTontineId}")
    public ResponseEntity<List<ContributionResponse>> getMemberHistory(
            @PathVariable Long memberTontineId
    ) {
        List<ContributionResponse> history = contributionService.getMemberHistory(memberTontineId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Historique global d'une tontine")
    @GetMapping("/tontine/{tontineId}")
    public ResponseEntity<List<ContributionResponse>> getTontineHistory(
            @PathVariable Long tontineId
    ) {
        List<ContributionResponse> history = contributionService.getTontineHistory(tontineId);
        return ResponseEntity.ok(history);
    }

    @Operation(
            summary = "Liste des membres en retard de paiement",
            description = "Retourne la liste des membres ayant des cotisations au statut LATE avec le montant total de leur dette.")
    @GetMapping("/tontine/{tontineId}/late-members")
    public ResponseEntity<List<LateMemberResponse>> getLateMembers(
            @PathVariable Long tontineId
    ) {
        List<LateMemberResponse> lates = contributionService.getLateMembers(tontineId);
        return ResponseEntity.ok(lates);
    }

    @Operation(summary = "Mettre à jour le statut d'une cotisation")
    @PatchMapping("/{id}/status/{status}/admin/{adminId}")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @PathVariable String status,
            @PathVariable Long adminId
    ) {
        contributionService.updateContributionStatus(id, status, adminId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Détails d'une cotisation")
    @GetMapping("/{id}")
    public ResponseEntity<ContributionResponse> getContributionById(@PathVariable Long id) {
        return ResponseEntity.ok(contributionService.getContributionById(id));
    }
}
