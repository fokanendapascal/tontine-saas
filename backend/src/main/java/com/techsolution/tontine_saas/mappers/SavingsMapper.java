package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.SavingsRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsResponse;
import com.techsolution.tontine_saas.entities.Savings;
import com.techsolution.tontine_saas.entities.User;

import java.math.BigDecimal;

public class SavingsMapper {

    public static Savings toEntity(SavingsRequest request, User user) {

        return Savings.builder()
                .balance(
                        request.getBalance() != null
                                ? request.getBalance()
                                : BigDecimal.ZERO
                )
                .user(user)
                .build();
    }

    public static SavingsResponse toResponse(
            Savings savings,
            Long totalDeposits,
            Long totalWithdrawals,
            BigDecimal totalSavedAmount,
            BigDecimal loanEligibilityThreshold
    ) {

        User user = savings.getUser();

        BigDecimal safeBalance =
                savings.getBalance() != null
                        ? savings.getBalance()
                        : BigDecimal.ZERO;

        BigDecimal safeTotalSaved =
                totalSavedAmount != null
                        ? totalSavedAmount
                        : BigDecimal.ZERO;

        boolean eligible =
                loanEligibilityThreshold != null &&
                        safeBalance.compareTo(loanEligibilityThreshold) >= 0;

        SavingsResponse.SavingsResponseBuilder builder =
                SavingsResponse.builder()
                        .id(savings.getId())
                        .balance(safeBalance)
                        .userId(user != null ? user.getId() : null)
                        .totalDeposits(totalDeposits != null ? totalDeposits : 0)
                        .totalWithdrawals(totalWithdrawals != null ? totalWithdrawals : 0)
                        .totalSavedAmount(safeTotalSaved)
                        .loanEligibilityThreshold(loanEligibilityThreshold)
                        .eligibleForLoan(eligible);

        if (user != null) {
            builder.userFullName(
                    user.getFirstName() + " " +
                            user.getLastName()
            );
        }

        return builder.build();
    }
}