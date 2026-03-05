package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.SavingsRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SavingsService {

    // Initialisation du compte épargne
    SavingsResponse createSavingsAccount(SavingsRequest request, Long adminId);

    // Dépôt d'argent
    SavingsResponse deposit(Long userId, BigDecimal amount, Long adminId);

    // Retrait d'argent
    SavingsResponse withdraw(Long userId, BigDecimal amount, Long adminId);

    // Consultation
    SavingsResponse getSavingsByUserId(Long userId);

    List<SavingsResponse> getAssociationSavings(Long associationId);
}
