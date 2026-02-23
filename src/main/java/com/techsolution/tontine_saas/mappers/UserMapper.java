package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.UserRequest;
import com.techsolution.tontine_saas.dtos.response.UserResponse;
import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.User;

public class UserMapper {

    public static User toEntity(
            UserRequest request,
            Association association,
            String encodedPassword
    ) {

        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(encodedPassword) // 🔐 hash déjà fait dans service
                .roles(request.getRoles())
                .preferredLanguage(
                        request.getPreferredLanguage() != null
                                ? request.getPreferredLanguage()
                                : "FR"
                )
                .active(request.getActive() != null ? request.getActive() : true)
                .association(association)
                .build();
    }

    public static UserResponse toResponse(
            User user,
            Integer totalTontinesParticipated,
            Boolean eligibleForLoan
    ) {

        Association association = user.getAssociation();

        boolean isAdmin =
                user.getRoles() != null &&
                        user.getRoles().stream()
                                .anyMatch(role -> role.name().equals("ADMIN"));

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .preferredLanguage(user.getPreferredLanguage())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .associationId(association != null ? association.getId() : null)
                .associationName(association != null ? association.getName() : null)
                .totalTontinesParticipated(
                        totalTontinesParticipated != null ? totalTontinesParticipated : 0
                )
                .eligibleForLoan(eligibleForLoan != null ? eligibleForLoan : false)
                .admin(isAdmin)
                .build();
    }
}