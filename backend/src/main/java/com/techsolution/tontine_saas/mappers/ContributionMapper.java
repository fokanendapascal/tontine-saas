package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.ContributionRequest;
import com.techsolution.tontine_saas.dtos.response.ContributionResponse;
import com.techsolution.tontine_saas.entities.Contribution;
import com.techsolution.tontine_saas.entities.MemberTontine;

import java.math.BigDecimal;

public class ContributionMapper {

    public static Contribution toEntity(ContributionRequest request, MemberTontine memberTontine) {
        return Contribution.builder()
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .paymentDate(request.getPaymentDate())
                .penalty(request.getPenalty() != null ? request.getPenalty() : BigDecimal.ZERO)
                .status(request.getStatus())
                .memberTontine(memberTontine)
                .build();
    }

    public static ContributionResponse toResponse(Contribution contribution, BigDecimal totalPaidForTontine) {
        ContributionResponse.ContributionResponseBuilder builder = ContributionResponse.builder()
                .id(contribution.getId())
                .amount(contribution.getAmount())
                .dueDate(contribution.getDueDate())
                .paymentDate(contribution.getPaymentDate())
                .penalty(contribution.getPenalty())
                .status(contribution.getStatus())
                .memberTontineId(contribution.getMemberTontine() != null ? contribution.getMemberTontine().getId() : null)
                .totalPaidForTontine(totalPaidForTontine != null ? totalPaidForTontine : BigDecimal.ZERO)
                .latePayment(
                        contribution.getPaymentDate() != null &&
                                contribution.getDueDate() != null &&
                                contribution.getPaymentDate().isAfter(contribution.getDueDate())
                );

        // 🔹 Extensions intelligentes
        if (contribution.getMemberTontine() != null) {
            if (contribution.getMemberTontine().getUser() != null) {
                builder.userFullName(
                        contribution.getMemberTontine().getUser().getFirstName() + " " +
                                contribution.getMemberTontine().getUser().getLastName()
                );
            }
            if (contribution.getMemberTontine().getTontine() != null) {
                builder.tontineName(contribution.getMemberTontine().getTontine().getName());
            }
        }

        return builder.build();
    }
}
