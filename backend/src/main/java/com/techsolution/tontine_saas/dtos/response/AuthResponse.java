package com.techsolution.tontine_saas.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse userResponse;
    private String tokenType = "Bearer";

}
