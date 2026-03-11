package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.dtos.response.DashboardStatsResponse;
import com.techsolution.tontine_saas.entities.ContributionStatus;
import com.techsolution.tontine_saas.entities.LoanStatus;
import com.techsolution.tontine_saas.repository.ContributionRepository;
import com.techsolution.tontine_saas.repository.LoanRepository;
import com.techsolution.tontine_saas.repository.SavingsRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import com.techsolution.tontine_saas.security.SecurityUtils;
import com.techsolution.tontine_saas.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ContributionRepository contributionRepository;
    private final SavingsRepository savingsRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getAssociationStats() {

        Long associationId = SecurityUtils.getCurrentAssociationId();

        // 1. Total des cotisations perçues (Status PAID)
        BigDecimal totalContributions = contributionRepository.sumAmountByAssociationAndStatus(
                associationId, ContributionStatus.PAID);

        // 2. Épargne globale (Somme des balances de tous les comptes)
        BigDecimal totalSavings = savingsRepository.sumBalanceByAssociationId(associationId);

        // 3. Dettes impayées (Somme des remainingAmount des prêts APPROVED/PARTIALLY_PAID)
        BigDecimal totalUnpaidLoans = loanRepository.sumRemainingAmountByAssociationId(
                associationId,
                List.of(LoanStatus.APPROVED, LoanStatus.PARTIALLY_PAID)
        );


        // 4. Montant des cotisations en retard (Status LATE)
        BigDecimal lateAmount = contributionRepository.sumAmountByAssociationAndStatus(
                associationId, ContributionStatus.LATE);

        // 5. Statistiques de volume
        Long activeMembers = userRepository.countByAssociationIdAndActiveTrue(associationId);
        Long pendingLoans = loanRepository.countByUser_Association_IdAndStatus(associationId, LoanStatus.PENDING);

        return DashboardStatsResponse.builder()
                .totalContributions(totalContributions != null ? totalContributions : BigDecimal.ZERO)
                .totalSavings(totalSavings != null ? totalSavings : BigDecimal.ZERO)
                .totalUnpaidLoans(totalUnpaidLoans != null ? totalUnpaidLoans : BigDecimal.ZERO)
                .lateContributionsAmount(lateAmount != null ? lateAmount : BigDecimal.ZERO)
                .activeMembersCount(activeMembers)
                .pendingLoansCount(pendingLoans)
                .build();
    }
}
