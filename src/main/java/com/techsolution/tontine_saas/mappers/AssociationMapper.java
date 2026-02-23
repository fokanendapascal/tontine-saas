package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.AssociationRequest;
import com.techsolution.tontine_saas.dtos.response.AssociationResponse;
import com.techsolution.tontine_saas.entities.Association;

public class AssociationMapper {

    public static Association toEntity(AssociationRequest request) {
        Association association = new Association();
        association.setName(request.getName());
        association.setCountry(request.getCountry());
        association.setCurrency(request.getCurrency());
        association.setActive(request.getActive() != null ? request.getActive() : true);
        return association;
    }

    public static AssociationResponse toResponse(
            Association association,
            Long numberOfUsers,
            Long numberOfTontines,
            Long activeUsers
    ) {
        return AssociationResponse.builder()
                .id(association.getId())
                .name(association.getName())
                .country(association.getCountry())
                .currency(association.getCurrency())
                .createdAt(association.getCreatedAt())
                .active(association.getActive())
                .numberOfUsers(numberOfUsers != null ? numberOfUsers : 0L)
                .numberOfTontines(numberOfTontines != null ? numberOfTontines : 0L)
                .activeUsers(activeUsers != null ? activeUsers : 0L)
                .build();
    }

}
