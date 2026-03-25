package com.techsolution.tontine_saas.exceptions;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    public BusinessException(String messageKey, Object... args) {
        super(
                messageKey,
                "BUSINESS_ERROR",
                HttpStatus.BAD_REQUEST,
                args
        );
    }

    public BusinessException(String messageKey,
                             HttpStatus status,
                             Object... args) {
        super(
                messageKey,
                "BUSINESS_ERROR",
                status,
                args
        );
    }
}
