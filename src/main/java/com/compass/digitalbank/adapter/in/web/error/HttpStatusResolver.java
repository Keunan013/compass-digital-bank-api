package com.compass.digitalbank.adapter.in.web.error;

import com.compass.digitalbank.domain.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;
import java.util.Map;

final class HttpStatusResolver {

    private static final Map<ErrorCode, HttpStatus> STATUS_BY_CODE = new EnumMap<>(ErrorCode.class);

    static {
        STATUS_BY_CODE.put(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
        STATUS_BY_CODE.put(ErrorCode.INSUFFICIENT_BALANCE, HttpStatus.UNPROCESSABLE_ENTITY);
        STATUS_BY_CODE.put(ErrorCode.INVALID_TRANSFER, HttpStatus.BAD_REQUEST);
        STATUS_BY_CODE.put(ErrorCode.INACTIVE_ACCOUNT, HttpStatus.UNPROCESSABLE_ENTITY);
        STATUS_BY_CODE.put(ErrorCode.EMAIL_ALREADY_USED, HttpStatus.CONFLICT);
        STATUS_BY_CODE.put(ErrorCode.FORBIDDEN_OPERATION, HttpStatus.FORBIDDEN);
        STATUS_BY_CODE.put(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
        STATUS_BY_CODE.put(ErrorCode.MALFORMED_REQUEST, HttpStatus.BAD_REQUEST);
        STATUS_BY_CODE.put(ErrorCode.DATA_CONFLICT, HttpStatus.CONFLICT);
        STATUS_BY_CODE.put(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        STATUS_BY_CODE.put(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        STATUS_BY_CODE.put(ErrorCode.RATE_LIMIT_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS);
        STATUS_BY_CODE.put(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatusResolver() {
    }

    static HttpStatus resolve(ErrorCode code) {
        return STATUS_BY_CODE.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
