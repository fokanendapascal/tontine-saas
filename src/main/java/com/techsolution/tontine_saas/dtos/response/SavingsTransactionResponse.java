package com.techsolution.tontine_saas.dtos.response;

import com.techsolution.tontine_saas.entities.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsTransactionResponse {

    private Long id;

    private BigDecimal amount;

    private TransactionType type;

    private String description;

    private LocalDateTime createdAt;

    private Long savingsId;

    // 🔥 Extensions intelligentes
    private BigDecimal balanceAfterTransaction;
    private BigDecimal previousBalance;
    private String userFullName;
    private Boolean successful;
}
