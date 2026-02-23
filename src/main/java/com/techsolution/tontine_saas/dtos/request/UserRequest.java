package com.techsolution.tontine_saas.dtos.request;

import com.techsolution.tontine_saas.entities.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    private String password;

    @NotEmpty(message = "At least one role is required")
    private List<Role> roles;

    @Size(max = 5)
    @Builder.Default
    private String preferredLanguage = "FR";

    @Builder.Default
    private Boolean active = true;

    @NotNull(message = "Association ID is required")
    private Long associationId;

    // 🔹 Extension intelligente
    @Builder.Default
    private Boolean sendWelcomeEmail = true;
}
