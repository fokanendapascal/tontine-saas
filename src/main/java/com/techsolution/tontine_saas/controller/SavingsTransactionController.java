package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.SavingsTransactionRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsTransactionResponse;
import com.techsolution.tontine_saas.services.SavingsTransactionService;
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
@RequestMapping("/api/v1/savings-transactions")
@Tag(name = "Savings Transactions" , description = "API de gestion des transactions d'épargne")
public class SavingsTransactionController {

    private final SavingsTransactionService savingsTransactionService;

    @Operation(summary = "Enregistrer une transaction", description = "Permet de traiter un dépôt ou un retrait sur un compte d'épargne.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction traitée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données de transaction invalides ou solde insuffisant")
    })
    @PostMapping("/admin/{adminId}")
    public ResponseEntity<SavingsTransactionResponse> processTransaction(
            @RequestBody SavingsTransactionRequest request,
            @PathVariable("adminId") Long adminId
    ){
        SavingsTransactionResponse savedTransaction = savingsTransactionService.processTransaction(request, adminId);
        return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Historique d'un compte",
            description = "Récupère toutes les transactions liées à un compte d'épargne spécifique.")
    @GetMapping("/savings-account/{savingsId}")
    public ResponseEntity<List<SavingsTransactionResponse>> getTransactionHistory(
            @PathVariable("savingsId") Long savingsId
    ){
        List<SavingsTransactionResponse> savingsTransactions = savingsTransactionService.getTransactionHistory(savingsId);
        return ResponseEntity.ok(savingsTransactions);
    }

    @Operation(
            summary = "Détails d'une transaction",
            description = "Récupère les détails d'une transaction spécifique via son ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction trouvée"),
            @ApiResponse(responseCode = "404", description = "Transaction introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SavingsTransactionResponse> getTransactionById(@PathVariable Long id){
        SavingsTransactionResponse savingsTransaction = savingsTransactionService.getTransactionById(id);
        return ResponseEntity.ok(savingsTransaction);
    }

}
