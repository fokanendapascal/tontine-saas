package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.MemberTontineRequest;
import com.techsolution.tontine_saas.dtos.response.MemberTontineResponse;
import com.techsolution.tontine_saas.services.MemberTontineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/members-tontines")
@Tag(name = "Members Tontines" , description = "API de gestion des membres des tontines")
public class MemberTontineController {

    private final MemberTontineService memberTontineService;

    @Operation(summary = "Ajouter un membre à une tontine", description = "Inscrit un utilisateur dans une tontine spécifique.")
    @PostMapping("/admin/{adminId}")
    public ResponseEntity<MemberTontineResponse> addMemberToTontine(
            @RequestBody MemberTontineRequest request,
            @PathVariable Long adminId
    ) {
        MemberTontineResponse response = memberTontineService.addMemberToTontine(request, adminId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Retirer un membre d'une tontine")
    @DeleteMapping("/{id}/admin/{adminId}")
    public ResponseEntity<Void> removeMemberFromTontine(
            @PathVariable Long id,
            @PathVariable Long adminId
    ) {
        memberTontineService.removeMemberFromTontine(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lister les membres d'une tontine")
    @GetMapping("/tontine/{tontineId}")
    public ResponseEntity<List<MemberTontineResponse>> getMembersByTontine(
            @PathVariable Long tontineId
    ) {
        List<MemberTontineResponse> members = memberTontineService.getMembersByTontine(tontineId);
        return ResponseEntity.ok(members);
    }

    @Operation(summary = "Lister les tontines d'un utilisateur")
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
