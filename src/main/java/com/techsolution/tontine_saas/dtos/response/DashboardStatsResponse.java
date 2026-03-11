package com.techsolution.tontine_saas.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DashboardStatsResponse {
    private BigDecimal totalContributions; // Cotisations perçues
    private BigDecimal totalSavings;      // Épargne globale actuelle
    private BigDecimal totalUnpaidLoans;   // Dettes impayées (Remaining amount total)
    private Long activeMembersCount;       // Nombre de membres actifs
    private Long pendingLoansCount;        // Prêts en attente d'approbation
    private BigDecimal lateContributionsAmount; // Montant total des retards
}
