package com.compass.digitalbank.domain.exception;

public class ForbiddenOperationException extends DomainException {

    public ForbiddenOperationException(String message) {
        super(ErrorCode.FORBIDDEN_OPERATION, message);
    }
}
