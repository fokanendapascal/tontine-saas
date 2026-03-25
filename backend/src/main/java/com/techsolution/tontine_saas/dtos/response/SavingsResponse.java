package com.techsolution.tontine_saas.dtos.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsResponse {

    private Long id;

    private BigDecimal balance;

    private Long userId;

    // 🔥 Extensions intelligentes
    private String userFullName;
    private Long totalDeposits;
    private Long totalWithdrawals;
    private BigDecimal totalSavedAmount;
    private Boolean eligibleForLoan;
    private BigDecimal loanEligibilityThreshold;
}