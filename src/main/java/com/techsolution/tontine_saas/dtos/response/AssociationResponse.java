package com.techsolution.tontine_saas.dtos.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationResponse {

    private Long id;

    private String name;

    private String country;

    private String currency;

    private LocalDateTime createdAt;

    private Boolean active;

    // Extensions intelligentes
    private Long numberOfUsers;
    private Long numberOfTontines;
    private Long activeUsers;
}
