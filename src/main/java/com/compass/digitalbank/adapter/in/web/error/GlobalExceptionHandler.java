package com.compass.digitalbank.adapter.in.web.error;

import com.compass.digitalbank.domain.exception.DomainException;
import com.compass.digitalbank.domain.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final TraceIdProvider traceIdProvider;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatusResolver.resolve(ex.getCode());
        if (status.is5xxServerError()) {
            log.error("Domain error [{}]", ex.getCode(), ex);
        } else {
            log.warn("Domain error [{}]: {}", ex.getCode(), ex.getMessage());
        }
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), ex.getCode().name(), ex.getMessage(),
                        request.getRequestURI(), traceIdProvider.currentTraceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toViolation)
                .toList();
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), ErrorCode.VALIDATION_ERROR.name(),
                "Request validation failed", request.getRequestURI(), traceIdProvider.currentTraceId(), violations);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleMalformedRequest(Exception ex, HttpServletRequest request) {
        log.warn("Malformed request on {}: {}", request.getRequestURI(), ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), ErrorCode.MALFORMED_REQUEST.name(),
                        "Malformed or unreadable request", request.getRequestURI(), traceIdProvider.currentTraceId()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), ErrorCode.DATA_CONFLICT.name(),
                        "Request conflicts with the current state of a resource",
                        request.getRequestURI(), traceIdProvider.currentTraceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error handling {}", request.getRequestURI(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), ErrorCode.INTERNAL_ERROR.name(),
                        "An unexpected error occurred", request.getRequestURI(), traceIdProvider.currentTraceId()));
    }

    private ApiError.FieldViolation toViolation(FieldError error) {
        return new ApiError.FieldViolation(error.getField(), error.getDefaultMessage());
    }
}
