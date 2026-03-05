package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.LoanRequest;
import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.LoanResponse;
import com.techsolution.tontine_saas.services.LoanService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/loans")
@Tag(name = "Loans", description = "API de gestion des prêts et crédits scolaires/sociaux")
public class LoanController {

    private final LoanService loanService;

    @Operation(
            summary = "Soumettre une demande de prêt",
            description = "Un membre soumet une demande de prêt avec montant et durée.")
    @PostMapping("/user/{userId}")
    public ResponseEntity<LoanResponse> createLoanRequest(
            @RequestBody LoanRequest loanRequest,
            @PathVariable Long userId
    ){
        LoanResponse response = loanService.createLoanRequest(loanRequest, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Approuver un prêt",
            description = "L'admin doit être identifié par son email dans le corps de la requête.")
    @PatchMapping("/{loanId}/approve")
    public ResponseEntity<LoanResponse> approveLoan(
            @PathVariable Long loanId,
            @RequestBody UserRequest adminRequest
    ){
        LoanResponse response = loanService.approveLoan(loanId, adminRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Rejeter un prêt")
    @PatchMapping("/{loanId}/reject")
    public ResponseEntity<LoanResponse> rejectLoan(
            @PathVariable Long loanId,
            @RequestBody UserRequest adminRequest
    ){
        LoanResponse response = loanService.rejectLoan(loanId, adminRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister les prêts d'un utilisateur")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponse>> getUserLoans(@PathVariable Long userId){
        List<LoanResponse> loans = loanService.getUserLoans(userId);
        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "Lister tous les prêts d'une association")
    @GetMapping("/association/{associationId}")
    public ResponseEntity<List<LoanResponse>> getAssociationLoans(@PathVariable Long associationId){
        List<LoanResponse> loans = loanService.getAssociationLoans(associationId);
        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "Détails d'un prêt spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prêt trouvé"),
            @ApiResponse(responseCode = "404", description = "Prêt introuvable")
    })
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long loanId){
        LoanResponse response = loanService.getLoanById(loanId);
        return ResponseEntity.ok(response);
    }


}
