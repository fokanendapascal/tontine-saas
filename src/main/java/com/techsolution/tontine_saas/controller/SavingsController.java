package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.SavingsRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsResponse;
import com.techsolution.tontine_saas.services.SavingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/savings")
@Tag(name = "Savings" , description = "API de gestion des épargnes")
public class SavingsController {

    private final SavingsService savingsService;

    @Operation(summary = "Initialiser un compte épargne pour un membre")
    @PostMapping("/admin/{adminId}")
    public ResponseEntity<SavingsResponse> createSavingsAccount(
            @RequestBody SavingsRequest request,
            @PathVariable Long adminId
    ) {
        SavingsResponse response = savingsService.createSavingsAccount(request, adminId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Effectuer un dépôt")
    @PostMapping("/deposit")
    public ResponseEntity<SavingsResponse> deposit(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam Long adminId
    ) {
        SavingsResponse response = savingsService.deposit(userId, amount, adminId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Effectuer un retrait")
    @PostMapping("/withdraw")
    public ResponseEntity<SavingsResponse> withdraw(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam Long adminId
    ) {
        SavingsResponse response = savingsService.withdraw(userId, amount, adminId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consulter le solde d'un membre")
    @GetMapping("/user/{userId}")
    public ResponseEntity<SavingsResponse> getSavingsByUserId(@PathVariable Long userId) {
        SavingsResponse response = savingsService.getSavingsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister toutes les épargnes d'une association")
    @GetMapping("/association/{associationId}")
    public ResponseEntity<List<SavingsResponse>> getAssociationSavings(@PathVariable Long associationId) {
        List<SavingsResponse> response = savingsService.getAssociationSavings(associationId);
        return ResponseEntity.ok(response);
    }

}
