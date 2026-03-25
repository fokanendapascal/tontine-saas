package com.techsolution.tontine_saas.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {

    public ValidationException(String messageKey, Object... args) {
        super(
                messageKey,
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST,
                args
        );
    }
}
