package com.compass.digitalbank.domain.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
