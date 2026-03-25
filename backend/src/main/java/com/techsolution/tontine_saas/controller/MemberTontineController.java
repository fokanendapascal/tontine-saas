package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.MemberTontineRequest;
import com.techsolution.tontine_saas.dtos.response.MemberTontineResponse;
import com.techsolution.tontine_saas.services.MemberTontineService;
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
@RequestMapping("/api/v1/members-tontines")
@RequiredArgsConstructor
@Tag(name = "Members Tontines", description = "API de gestion des inscriptions et adhésions aux tontines")
public class MemberTontineController {

    private final MemberTontineService memberTontineService;

    @Operation(summary = "Ajouter un membre à une tontine",
            description = "Inscrit un utilisateur dans une tontine. L'ID de l'admin est récupéré via le token.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberTontineResponse> addMemberToTontine(
            @RequestBody MemberTontineRequest request
    ) {
        // Le service utilise SecurityUtils.getCurrentUserId() pour l'audit
        MemberTontineResponse response = memberTontineService.addMemberToTontine(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Retirer un membre d'une tontine",
            description = "Supprime l'adhésion si aucune contribution n'a été effectuée.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeMemberFromTontine(
            @PathVariable Long id
    ) {
        memberTontineService.removeMemberFromTontine(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lister les membres d'une tontine",
            description = "Récupère tous les inscrits d'une tontine spécifique (filtré par association).")
    @GetMapping("/tontine/{tontineId}")
    public ResponseEntity<List<MemberTontineResponse>> getMembersByTontine(
            @PathVariable Long tontineId
    ) {
        List<MemberTontineResponse> members = memberTontineService.getMembersByTontine(tontineId);
        return ResponseEntity.ok(members);
    }

    @Operation(summary = "Lister les tontines d'un utilisateur",
            description = "Permet de voir à quelles tontines un membre participe.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MemberTontineResponse>> getTontinesByUser(
            @PathVariable Long userId
    ) {
        List<MemberTontineResponse> tontines = memberTontineService.getTontinesByUser(userId);
        return ResponseEntity.ok(tontines);
    }

    @Operation(summary = "Obtenir les détails d'adhésion d'un membre")
    @GetMapping("/user/{userId}/tontine/{tontineId}")
    public ResponseEntity<MemberTontineResponse> getMemberDetails(
            @PathVariable Long userId,
            @PathVariable Long tontineId
    ) {
        MemberTontineResponse response = memberTontineService.getMemberDetails(userId, tontineId);
        return ResponseEntity.ok(response);
    }

}
