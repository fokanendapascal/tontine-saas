package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.MemberTontineRequest;
import com.techsolution.tontine_saas.dtos.response.MemberTontineResponse;
import com.techsolution.tontine_saas.entities.MemberTontine;
import com.techsolution.tontine_saas.entities.Tontine;
import com.techsolution.tontine_saas.entities.User;

import java.math.BigDecimal;

public class MemberTontineMapper {

    public static MemberTontine toEntity(
            MemberTontineRequest request,
            User user,
            Tontine tontine
    ) {

        return MemberTontine.builder()
                .orderPosition(request.getOrderPosition())
                .user(user)
                .tontine(tontine)
                .build();
    }

    public static MemberTontineResponse toResponse(
            MemberTontine entity,
            Long contributionCount,
            BigDecimal totalContributed
    ) {

        User user = entity.getUser();
        Tontine tontine = entity.getTontine();

        MemberTontineResponse.MemberTontineResponseBuilder builder =
                MemberTontineResponse.builder()
                        .id(entity.getId())
                        .orderPosition(entity.getOrderPosition())
                        .joinedAt(entity.getJoinedAt())
                        .userId(user != null ? user.getId() : null)
                        .tontineId(tontine != null ? tontine.getId() : null)
                        .contributionCount(contributionCount != null ? contributionCount : 0L)
                        .totalContributed(totalContributed != null ? totalContributed : BigDecimal.ZERO);

        if (user != null) {
            builder.userFullName(
                    user.getFirstName() + " " + user.getLastName()
            );
        }

        if (tontine != null) {
            builder.tontineName(tontine.getName());
        }

        // 🔹 Logique intelligente pour isActive
        boolean active =
                tontine != null &&
                        Boolean.TRUE.equals(tontine.getActive());

        builder.isActive(active);

        return builder.build();
    }
}