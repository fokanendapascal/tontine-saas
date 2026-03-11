package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.LoanRequest;
import com.techsolution.tontine_saas.dtos.response.LoanResponse;
import com.techsolution.tontine_saas.services.LoanService;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/loans")
@Tag(name = "Loans", description = "API de gestion des prêts et crédits scolaires/sociaux")
public class LoanController {

    private final LoanService loanService;

    @Operation(
            summary = "Soumettre une demande de prêt",
            description = "Un membre soumet sa demande. Son identité est extraite du token JWT.")
    @PostMapping("/request")
    public ResponseEntity<LoanResponse> createLoanRequest(
            @RequestBody LoanRequest loanRequest
    ){
        // Le service utilise SecurityUtils.getCurrentUserId()
        LoanResponse response = loanService.createLoanRequest(loanRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Approuver un prêt",
            description = "Réservé à l'administrateur de l'association. Met à jour le solde restant avec intérêts.")
    @PatchMapping("/{loanId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> approveLoan(
            @PathVariable Long loanId
    ){
        LoanResponse response = loanService.approveLoan(loanId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Rejeter un prêt", description = "L'administrateur rejette la demande.")
    @PatchMapping("/{loanId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> rejectLoan(
            @PathVariable Long loanId
    ){
        LoanResponse response = loanService.rejectLoan(loanId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister mes prêts", description = "Un membre consulte ses propres demandes.")
    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanResponse>> getUserLoans(){
        // Filtre automatique par l'ID utilisateur du token
        List<LoanResponse> loans = loanService.getUserLoans();
        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "Lister tous les prêts de l'association",
            description = "Réservé à l'admin. Liste tous les prêts de son association.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoanResponse>> getAssociationLoans(){
        // Filtre automatique par l'ID association du token
        List<LoanResponse> loans = loanService.getAssociationLoans();
        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "Détails d'un prêt spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prêt trouvé"),
            @ApiResponse(responseCode = "403", description = "Accès refusé (association différente)"),
            @ApiResponse(responseCode = "404", description = "Prêt introuvable")
    })
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long loanId){
        LoanResponse response = loanService.getLoanById(loanId);
        return ResponseEntity.ok(response);
    }
}
