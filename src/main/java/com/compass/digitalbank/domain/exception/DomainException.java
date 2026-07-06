package com.compass.digitalbank.domain.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

    private final ErrorCode code;

    protected DomainException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }
}
