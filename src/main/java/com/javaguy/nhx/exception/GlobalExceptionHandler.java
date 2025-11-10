package com.javaguy.nhx.exception;

import com.javaguy.nhx.model.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            InvalidOtpException.class,
            AccountDisabledException.class,
            EmailAlreadyExistsException.class,
            EmailNotVerifiedException.class,
            InsufficientAmountException.class,
            InvalidCredentialsException.class,
            InvalidTokenException.class,
            KycNotVerifiedException.class,
            OtpDeliveryException.class,
            PaymentFailedException.class,
            UserNotFoundException.class,
            ValidationException.class,
            TokenExpiredException.class,
            StorageException.class,
            KycAlreadyVerifiedException.class,
            InvalidDocumentException.class,
            WalletAlreadyWhitelistedException.class,
            InvalidAgeException.class,
            InvalidMintAmountException.class,
            WalletMismatchException.class,
            EmailServiceException.class
    })
    public ResponseEntity<ApiErrorResponse> handleCustomExceptions(
            BaseException ex, HttpServletRequest request) {
        log.error("Custom exception caught: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Validation error: {}", message, ex);
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.error("Constraint violation: {}", message, ex);
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Malformed JSON request. Please check the request body.";
        log.error("Http message not readable: {}", ex.getMessage(), ex);
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: ", ex);
        return buildErrorResponse("An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            String message, HttpStatus status, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
