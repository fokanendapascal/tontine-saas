package com.techsolution.tontine_saas.dtos.response;

import com.techsolution.tontine_saas.entities.TontineFrequency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TontineResponse {

    private Long id;

    private String name;

    private BigDecimal contributionAmount;

    private TontineFrequency frequency;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean active;

    private Long associationId;

    // 🔥 Extensions intelligentes
    private String associationName;
    private Long totalMembers;
    private BigDecimal totalCollectedAmount;
    private Boolean cycleCompleted;
    private BigDecimal expectedTotalAmount;
}