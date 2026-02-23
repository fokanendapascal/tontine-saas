package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.TontineRequest;
import com.techsolution.tontine_saas.dtos.response.TontineResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.Tontine;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TontineMapper {

    public static Tontine toEntity(
            TontineRequest request,
            Association association
    ) {

        return Tontine.builder()
                .name(request.getName())
                .contributionAmount(request.getContributionAmount())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() != null ? request.getActive() : true)
                .association(association)
                .build();
    }

    public static TontineResponse toResponse(
            Tontine tontine,
            Long totalMembers,
            BigDecimal totalCollectedAmount
    ) {

        Association association = tontine.getAssociation();

        Long safeMembers = totalMembers != null ? totalMembers : 0;
        BigDecimal safeCollected = totalCollectedAmount != null ? totalCollectedAmount : BigDecimal.ZERO;

        BigDecimal expectedTotal =
                tontine.getContributionAmount() != null
                        ? tontine.getContributionAmount().multiply(BigDecimal.valueOf(safeMembers))
                        : BigDecimal.ZERO;

        boolean cycleCompleted =
                tontine.getEndDate() != null &&
                        tontine.getEndDate().isBefore(LocalDate.now());

        TontineResponse.TontineResponseBuilder builder =
                TontineResponse.builder()
                        .id(tontine.getId())
                        .name(tontine.getName())
                        .contributionAmount(tontine.getContributionAmount())
                        .frequency(tontine.getFrequency())
                        .startDate(tontine.getStartDate())
                        .endDate(tontine.getEndDate())
                        .active(tontine.getActive())
                        .associationId(association != null ? association.getId() : null)
                        .totalMembers(safeMembers)
                        .totalCollectedAmount(safeCollected)
                        .expectedTotalAmount(expectedTotal)
                        .cycleCompleted(cycleCompleted);

        if (association != null) {
            builder.associationName(association.getName());
        }

        return builder.build();
    }
}