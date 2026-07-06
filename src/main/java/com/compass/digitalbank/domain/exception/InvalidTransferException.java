package com.compass.digitalbank.domain.exception;

public class InvalidTransferException extends DomainException {

    public InvalidTransferException(String message) {
        super(ErrorCode.INVALID_TRANSFER, message);
    }
}
