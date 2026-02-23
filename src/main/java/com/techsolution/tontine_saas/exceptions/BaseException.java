package com.techsolution.tontine_saas.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException{

    private final String messageKey;
    private final Object[] args;
    private final HttpStatus status;
    private final String errorCode;

    public BaseException(String messageKey,
                         String errorCode,
                         HttpStatus status,
                         Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
        this.status = status;
        this.errorCode = errorCode;
    }

}
