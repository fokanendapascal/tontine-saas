package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.LoanRepaymentRequest;
import com.techsolution.tontine_saas.dtos.response.LoanRepaymentResponse;
import com.techsolution.tontine_saas.services.LoanRepaymentService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/repayments")
@RequiredArgsConstructor
@Tag(name = "Loan Repayments", description = "API de suivi et gestion des remboursements de prêts")
public class LoanRepaymentController {

    private final LoanRepaymentService loanRepaymentService;

    @Operation(
            summary = "Enregistrer un nouveau remboursement",
            description = "Réservé aux administrateurs. Calcule automatiquement le nouveau solde restant (remainingAmount) du prêt.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanRepaymentResponse> recordRepayment(
            @Valid @RequestBody LoanRepaymentRequest request
    ) {
        // L'ID de l'admin et de l'association sont extraits du SecurityContext par le service
        LoanRepaymentResponse response = loanRepaymentService.recordRepayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Historique des remboursements d'un prêt spécifique")
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<LoanRepaymentResponse>> getRepaymentsByLoan(
            @PathVariable Long loanId
    ) {
        // Le service vérifie que le prêt appartient bien à l'association de l'utilisateur connecté
        List<LoanRepaymentResponse> repayments = loanRepaymentService.getRepaymentsByLoan(loanId);
        return ResponseEntity.ok(repayments);
    }

    @Operation(summary = "Détails d'un reçu de remboursement spécifique")
    @GetMapping("/{id}")
    public ResponseEntity<LoanRepaymentResponse> getRepaymentById(
            @PathVariable Long id
    ) {
        LoanRepaymentResponse response = loanRepaymentService.getRepaymentById(id);
        return ResponseEntity.ok(response);
    }
}
