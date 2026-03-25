package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.request.SavingsRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsResponse;
import com.techsolution.tontine_saas.services.SavingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/savings")
@RequiredArgsConstructor // Meilleure pratique que AllArgsConstructor pour l'injection
@Tag(name = "Savings", description = "API de gestion des comptes épargne et des mouvements de fonds")
public class SavingsController {

    private final SavingsService savingsService;

    @Operation(summary = "Initialiser un compte épargne pour un membre",
            description = "Réservé aux admins. L'association est détectée via le token.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SavingsResponse> createSavingsAccount(
            @RequestBody SavingsRequest request
    ) {
        SavingsResponse response = savingsService.createSavingsAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Effectuer un dépôt sur le compte d'un membre")
    @PostMapping("/deposit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SavingsResponse> deposit(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount
    ) {
        SavingsResponse response = savingsService.deposit(userId, amount);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Effectuer un retrait sur le compte d'un membre")
    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SavingsResponse> withdraw(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount
    ) {
        SavingsResponse response = savingsService.withdraw(userId, amount);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consulter le compte épargne d'un utilisateur spécifique")
    @GetMapping("/user/{userId}")
    public ResponseEntity<SavingsResponse> getSavingsByUserId(@PathVariable Long userId) {
        // Le service vérifiera si l'appelant a le droit de voir ce compte (même asso)
        SavingsResponse response = savingsService.getSavingsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lister tous les comptes épargne de l'association connectée")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SavingsResponse>> getAssociationSavings() {
        // L'ID association est récupéré dans SecurityUtils au niveau du service
        List<SavingsResponse> response = savingsService.getAssociationSavings();
        return ResponseEntity.ok(response);
    }

}
