package com.compass.digitalbank.domain.exception;

public class EmailAlreadyUsedException extends DomainException {

    public EmailAlreadyUsedException() {
        super(ErrorCode.EMAIL_ALREADY_USED, "Email is already registered");
    }
}
