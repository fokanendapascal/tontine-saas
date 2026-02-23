package com.techsolution.tontine_saas.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationRequest {

    @NotBlank(message = "Association name is required")
    private String name;

    private String country;

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String currency;

    /**
     * Active by default
     */
    private Boolean active;
}
