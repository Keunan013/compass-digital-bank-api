package com.compass.digitalbank.domain.exception;

public class InactiveAccountException extends DomainException {

    public InactiveAccountException() {
        super(ErrorCode.INACTIVE_ACCOUNT, "Transfer involves an inactive account");
    }
}
