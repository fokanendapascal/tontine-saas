package com.techsolution.tontine_saas.dtos.response;

import com.techsolution.tontine_saas.entities.LoanStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {

    private Long id;

    private BigDecimal amount;

    private Double interestRate;

    private LocalDate startDate;
    private LocalDate endDate;

    private LoanStatus status;

    private Long userId;

    // 🔥 Extensions intelligentes
    private String userFullName;
    private BigDecimal totalRepaid;
    private BigDecimal remainingAmount;  // 💡 très utile côté frontend
    private Boolean overdue;
}
