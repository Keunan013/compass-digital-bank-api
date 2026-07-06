package com.compass.digitalbank.adapter.in.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String traceId,
        List<FieldViolation> errors
) {

    public static ApiError of(int status, String code, String message, String path, String traceId) {
        return new ApiError(Instant.now(), status, code, message, path, traceId, null);
    }

    public static ApiError of(int status, String code, String message, String path, String traceId,
                              List<FieldViolation> errors) {
        return new ApiError(Instant.now(), status, code, message, path, traceId, errors);
    }

    public record FieldViolation(String field, String message) {
    }
}
