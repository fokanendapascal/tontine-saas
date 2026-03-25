package com.techsolution.tontine_saas.dtos.response;

import com.techsolution.tontine_saas.entities.ContributionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionResponse {

    private Long id;

    private BigDecimal amount;

    private LocalDateTime dueDate;

    private LocalDateTime paymentDate;

    private BigDecimal penalty;

    private ContributionStatus status;

    private Long memberTontineId;

    // 🔥 Extensions intelligentes
    private String userFullName;           // Prénom + Nom du membre
    private String tontineName;            // Nom de la tontine
    private Boolean latePayment;           // True si paymentDate > dueDate
    private BigDecimal totalPaidForTontine; // Total payé par ce membre dans la tontine
}