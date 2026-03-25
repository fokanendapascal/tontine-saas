package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.SavingsTransactionRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsTransactionResponse;
import com.techsolution.tontine_saas.entities.Savings;
import com.techsolution.tontine_saas.entities.SavingsTransaction;
import com.techsolution.tontine_saas.entities.User;

import java.math.BigDecimal;

public class SavingsTransactionMapper {

    public static SavingsTransaction toEntity(
            SavingsTransactionRequest request,
            Savings savings
    ) {

        return SavingsTransaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .savings(savings)
                .build();
    }

    public static SavingsTransactionResponse toResponse(
            SavingsTransaction tx,
            BigDecimal previousBalance,
            BigDecimal balanceAfterTransaction,
            Boolean successful
    ) {

        Savings savings = tx.getSavings();
        User user = savings != null ? savings.getUser() : null;

        SavingsTransactionResponse.SavingsTransactionResponseBuilder builder =
                SavingsTransactionResponse.builder()
                        .id(tx.getId())
                        .amount(tx.getAmount())
                        .type(tx.getType())
                        .description(tx.getDescription())
                        .createdAt(tx.getCreatedAt())
                        .savingsId(savings != null ? savings.getId() : null)
                        .previousBalance(previousBalance != null ? previousBalance : BigDecimal.ZERO)
                        .balanceAfterTransaction(balanceAfterTransaction != null ? balanceAfterTransaction : BigDecimal.ZERO)
                        .successful(successful != null ? successful : true);

        if (user != null) {
            builder.userFullName(
                    user.getFirstName() + " " +
                            user.getLastName()
            );
        }

        return builder.build();
    }
}