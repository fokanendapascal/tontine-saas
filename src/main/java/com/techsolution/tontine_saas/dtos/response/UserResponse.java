package com.techsolution.tontine_saas.dtos.response;

import com.techsolution.tontine_saas.entities.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String phone;

    private List<Role> roles;

    private String preferredLanguage;

    private Boolean active;

    private LocalDateTime createdAt;

    private Long associationId;

    // 🔥 Extensions intelligentes
    private String associationName;
    private Integer totalTontinesParticipated;
    private Boolean eligibleForLoan;
    private Boolean admin;
}