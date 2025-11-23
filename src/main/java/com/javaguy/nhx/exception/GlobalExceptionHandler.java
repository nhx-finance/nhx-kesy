package com.javaguy.nhx.exception;

import com.javaguy.nhx.model.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.exception.custom.BadRequestException;
import com.javaguy.nhx.exception.custom.UnauthorizedException;
import com.javaguy.nhx.exception.custom.ForbiddenException;
import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.exception.custom.ValidationException;
import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.KycNotVerifiedException;
import com.javaguy.nhx.exception.custom.AccountDisabledException;
import com.javaguy.nhx.exception.custom.BaseException;
import com.javaguy.nhx.exception.custom.ServiceUnavailableException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Helper method to create error response with consistent structure
     */
    private ErrorResponse buildErrorResponse(HttpStatus status, String title, String message, String path) {
        return new ErrorResponse(status, title, message, path);
    }

    /**
     * Helper method to log and return error response
     */
    private ResponseEntity<ErrorResponse> logAndRespond(HttpStatus status, String title, String message,
                                                        String path, Exception ex, boolean isError) {
        if (isError) {
            log.error("{}: {} | Path: {}", title, message, path, ex);
        } else {
            log.warn("{}: {} | Path: {}", title, message, path);
        }
        ErrorResponse errorResponse = buildErrorResponse(status, title, message, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    // ==================== Custom Application Exceptions ====================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "The requested resource was not found";
        return logAndRespond(HttpStatus.NOT_FOUND, "Resource Not Found", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "The request contains invalid data";
        return logAndRespond(HttpStatus.BAD_REQUEST, "Bad Request", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Authentication credentials are missing or invalid";
        return logAndRespond(HttpStatus.UNAUTHORIZED, "Unauthorized", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "You do not have permission to access this resource";
        return logAndRespond(HttpStatus.FORBIDDEN, "Forbidden", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "The request conflicts with existing data";
        return logAndRespond(HttpStatus.CONFLICT, "Conflict", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Validation failed for the provided data";
        return logAndRespond(HttpStatus.BAD_REQUEST, "Validation Error", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerException(InternalServerException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An internal server error occurred";
        return logAndRespond(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message, request.getRequestURI(), ex, true);
    }

    @ExceptionHandler(KycNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleKycNotVerifiedException(KycNotVerifiedException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Your KYC verification is pending or has not been completed. Please complete KYC to proceed.";
        return logAndRespond(HttpStatus.BAD_REQUEST, "KYC Verification Required", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabledException(AccountDisabledException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Your account has been disabled. Please contact support for assistance.";
        return logAndRespond(HttpStatus.FORBIDDEN, "Account Disabled", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        String title = ex.getStatus().getReasonPhrase();
        String message = ex.getMessage() != null ? ex.getMessage() : "An error occurred while processing your request";
        return logAndRespond(ex.getStatus(), title, message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "The service is temporarily unavailable. Please try again later.";
        return logAndRespond(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", message, request.getRequestURI(), ex, true);
    }

    // ==================== Spring & Jakarta Validation Exceptions ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (errorMessage.isEmpty()) {
            errorMessage = "One or more fields have validation errors";
        }

        return logAndRespond(HttpStatus.BAD_REQUEST, "Validation Error", errorMessage, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        if (errorMessage.isEmpty()) {
            errorMessage = "A constraint violation occurred";
        }

        return logAndRespond(HttpStatus.BAD_REQUEST, "Constraint Violation", errorMessage, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Required parameter '%s' (type: %s) is missing", ex.getParameterName(), ex.getParameterType());
        return logAndRespond(HttpStatus.BAD_REQUEST, "Missing Parameter", message, request.getRequestURI(), ex, false);
    }

    // ==================== HTTP Method & Media Type Exceptions ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());
        return logAndRespond(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("Content-Type '%s' is not supported. Supported types: %s",
                ex.getContentType(), ex.getSupportedMediaTypes());
        return logAndRespond(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", message, request.getRequestURI(), ex, false);
    }

    // ==================== Request/Response Parsing Exceptions ====================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Malformed JSON request body. Please ensure the JSON is valid and well-formed.";
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            message += " Details: " + cause.getMessage();
        }
        return logAndRespond(HttpStatus.BAD_REQUEST, "Invalid Request Format", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("Parameter '%s' has invalid value '%s'. Expected type: %s (e.g., '%s')",
                ex.getName(), ex.getValue(), expectedType, getExampleValue(expectedType));
        return logAndRespond(HttpStatus.BAD_REQUEST, "Invalid Parameter Type", message, request.getRequestURI(), ex, false);
    }

    // ==================== Security Exceptions ====================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "You do not have permission to access this resource";
        return logAndRespond(HttpStatus.FORBIDDEN, "Access Denied", message, request.getRequestURI(), ex, false);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Authentication failed. Please provide valid credentials.";
        return logAndRespond(HttpStatus.UNAUTHORIZED, "Authentication Failed", message, request.getRequestURI(), ex, false);
    }

    // ==================== Database Exceptions ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Data integrity violation. This could be a duplicate entry or a foreign key constraint violation.";

        String exceptionMessage = ex.getMessage();
        if (exceptionMessage != null) {
            if (exceptionMessage.contains("Unique constraint")) {
                message = "This record already exists. Please use a unique value.";
            } else if (exceptionMessage.contains("Foreign key constraint")) {
                message = "Cannot perform this operation due to related records in the system.";
            } else if (exceptionMessage.contains("NOT NULL constraint")) {
                message = "One or more required fields are missing.";
            }
        }

        return logAndRespond(HttpStatus.CONFLICT, "Data Integrity Violation", message, request.getRequestURI(), ex, false);
    }

    // ==================== Fallback Exception Handler ====================

    /**
     * Catch-all handler for any uncaught exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Uncaught Exception: {} | Message: {} | Path: {}",
                ex.getClass().getName(), ex.getMessage(), request.getRequestURI(), ex);

        String message = "An unexpected error occurred. Please try again later or contact support if the problem persists.";
        return logAndRespond(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message, request.getRequestURI(), ex, true);
    }

    // ==================== Utility Methods ====================

    /**
     * Returns an example value for a given type for error messages
     */
    private String getExampleValue(String type) {
        return switch (type) {
            case "Integer", "int" -> "123";
            case "Long", "long" -> "123456789";
            case "Double", "double", "Float", "float" -> "123.45";
            case "Boolean", "boolean" -> "true";
            case "LocalDate" -> "2025-11-23";
            case "LocalDateTime" -> "2025-11-23T10:30:00";
            case "UUID" -> "550e8400-e29b-41d4-a716-446655440000";
            default -> type.toLowerCase();
        };
    }
}
