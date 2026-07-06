package com.compass.digitalbank.adapter.in.web.dto;

public final class ValidationConstants {

    public static final int NAME_MAX = 150;
    public static final int EMAIL_MAX = 180;
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String EMAIL_PATTERN_MESSAGE = "email must be a valid address";
    public static final int PASSWORD_MIN = 10;
    public static final int PASSWORD_MAX = 72;
    public static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$";
    public static final String PASSWORD_PATTERN_MESSAGE =
            "password must contain lowercase, uppercase, a digit and a special character";
    public static final int ACCOUNT_NAME_MAX = 150;

    public static final int AMOUNT_INTEGER_DIGITS = 17;
    public static final int AMOUNT_FRACTION_DIGITS = 2;
    public static final String MIN_TRANSFER_AMOUNT = "0.01";
    public static final String MIN_INITIAL_BALANCE = "0.00";

    private ValidationConstants() {
    }
}
