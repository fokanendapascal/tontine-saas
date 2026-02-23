package com.techsolution.tontine_saas.exceptions;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BaseException{

    public AuthorizationException(String messageKey, Object... args) {
        super(
                messageKey,
                "AUTHORIZATION_ERROR",
                HttpStatus.FORBIDDEN,
                args
        );
    }

}
