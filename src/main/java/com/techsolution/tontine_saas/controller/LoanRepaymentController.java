package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.LoanRepaymentRequest;
import com.techsolution.tontine_saas.dtos.response.LoanRepaymentResponse;
import com.techsolution.tontine_saas.services.LoanRepaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/repayments")
@RequiredArgsConstructor
@Tag(name = "Loan Repayments", description = "API de suivi des remboursements de prêts")
public class LoanRepaymentController {

    private final LoanRepaymentService loanrepaymentService;

    @Operation(summary = "Enregistrer un nouveau remboursement",
            description = "Enregistre un paiement pour un prêt spécifique. Met à jour automatiquement le statut du prêt.")
    @PostMapping("/admin/{adminId}")
    public ResponseEntity<LoanRepaymentResponse> recordRepayment(
            @RequestBody LoanRepaymentRequest request,
            @PathVariable Long adminId
    ) {
        LoanRepaymentResponse response = loanrepaymentService.recordRepayment(request, adminId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Historique des remboursements d'un prêt")
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<LoanRepaymentResponse>> getRepaymentsByLoan(
            @PathVariable Long loanId
    ) {
        List<LoanRepaymentResponse> repayments = loanrepaymentService.getRepaymentsByLoan(loanId);
        return ResponseEntity.ok(repayments);
    }

    @Operation(summary = "Détails d'un remboursement spécifique")
    @GetMapping("/{id}")
    public ResponseEntity<LoanRepaymentResponse> getRepaymentById(
            @PathVariable Long id
    ) {
        LoanRepaymentResponse response = loanrepaymentService.getRepaymentById(id);
        return ResponseEntity.ok(response);
    }
}
