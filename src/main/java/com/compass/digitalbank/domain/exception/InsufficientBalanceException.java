package com.compass.digitalbank.domain.exception;

public class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_BALANCE,
                "Source account does not have enough balance for this transfer");
    }
}
