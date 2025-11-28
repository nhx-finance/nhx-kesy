package com.javaguy.nhx.exception;

import com.javaguy.nhx.exception.custom.ResourceNotFoundException;
import com.javaguy.nhx.exception.custom.BadRequestException;
import com.javaguy.nhx.exception.custom.UnauthorizedException;
import com.javaguy.nhx.exception.custom.ForbiddenException;
import com.javaguy.nhx.exception.custom.ConflictException;
import com.javaguy.nhx.exception.custom.ValidationException;
import com.javaguy.nhx.exception.custom.InternalServerException;
import com.javaguy.nhx.exception.custom.KycNotVerifiedException;
import com.javaguy.nhx.exception.custom.AccountDisabledException;
import com.javaguy.nhx.exception.custom.ServiceUnavailableException;
import com.javaguy.nhx.exception.custom.BaseException;
import com.javaguy.nhx.model.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import java.util.Collections;
import java.util.Set;
import org.springframework.http.HttpInputMessage;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleResourceNotFoundException(ex,
                request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(404, responseEntity.getBody().getStatus());
        assertEquals("Resource Not Found", responseEntity.getBody().getError());
        assertEquals("Resource not found test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad request test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleBadRequestException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("Bad Request", responseEntity.getBody().getError());
        assertEquals("Bad request test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUnauthorizedException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(401, responseEntity.getBody().getStatus());
        assertEquals("Unauthorized", responseEntity.getBody().getError());
        assertEquals("Unauthorized test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Forbidden test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleForbiddenException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(403, responseEntity.getBody().getStatus());
        assertEquals("Forbidden", responseEntity.getBody().getError());
        assertEquals("Forbidden test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleConflictException() {
        ConflictException ex = new ConflictException("Conflict test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleConflictException(ex, request);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(409, responseEntity.getBody().getStatus());
        assertEquals("Conflict", responseEntity.getBody().getError());
        assertEquals("Conflict test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleValidationException() {
        ValidationException ex = new ValidationException("Validation test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("Validation Error", responseEntity.getBody().getError());
        assertEquals("Validation test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleInternalServerException() {
        InternalServerException ex = new InternalServerException("Internal server error test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleInternalServerException(ex,
                request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(500, responseEntity.getBody().getStatus());
        assertEquals("Internal Server Error", responseEntity.getBody().getError());
        assertEquals("Internal server error test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    // Spring & Jakarta Exceptions

    @Test
    void handleConstraintViolationException() {
        ConstraintViolation<?> constraintViolation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        when(constraintViolation.getMessage()).thenReturn("must not be null");
        when(propertyPath.toString()).thenReturn("fieldName");

        Set<ConstraintViolation<?>> violations = Collections.singleton(constraintViolation);
        ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);

        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("Constraint Violation", responseEntity.getBody().getError());
        assertEquals("fieldName: must not be null", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleMissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param", "String");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleMissingServletRequestParameter(ex,
                request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("Missing Parameter", responseEntity.getBody().getError());
        assertNotNull(responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("GET");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleHttpRequestMethodNotSupported(ex,
                request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(405, responseEntity.getBody().getStatus());
        assertEquals("Method Not Allowed", responseEntity.getBody().getError());
        assertNotNull(responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleHttpMessageNotReadableException() {
        HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error", httpInputMessage);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleHttpMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("Invalid Request Format", responseEntity.getBody().getError());
        assertEquals("Malformed JSON request body. Please ensure the JSON is valid and well-formed.",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("value", String.class, "name",
                null, new IllegalArgumentException());
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleMethodArgumentTypeMismatch(ex,
                request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("Invalid Parameter Type", responseEntity.getBody().getError());
        assertNotNull(responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleHttpMediaTypeNotSupportedException() {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException("application/xml");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleHttpMediaTypeNotSupported(ex,
                request);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(415, responseEntity.getBody().getStatus());
        assertEquals("Unsupported Media Type", responseEntity.getBody().getError());
        assertNotNull(responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAccessDeniedException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(403, responseEntity.getBody().getStatus());
        assertEquals("Access Denied", responseEntity.getBody().getError());
        assertEquals("Access denied test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Authentication failed test") {
        };
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAuthenticationException(ex,
                request);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(401, responseEntity.getBody().getStatus());
        assertEquals("Authentication Failed", responseEntity.getBody().getError());
        assertEquals("Authentication failed test", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Data integrity violation test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(409, responseEntity.getBody().getStatus());
        assertEquals("Data Integrity Violation", responseEntity.getBody().getError());
        assertEquals("Data integrity violation. This could be a duplicate entry or a foreign key constraint violation.",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleAllUncaughtException() {
        Exception ex = new Exception("Generic error test");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAllUncaughtException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(500, responseEntity.getBody().getStatus());
        assertEquals("Internal Server Error", responseEntity.getBody().getError());
        assertEquals("An unexpected error occurred. Please try again later or contact support if the problem persists.",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleKycNotVerifiedException() {
        KycNotVerifiedException ex = new KycNotVerifiedException("KYC verification pending");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleKycNotVerifiedException(ex,
                request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("KYC Verification Required", responseEntity.getBody().getError());
        assertEquals("KYC verification pending", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleAccountDisabledException() {
        AccountDisabledException ex = new AccountDisabledException("Account has been disabled");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAccountDisabledException(ex,
                request);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(403, responseEntity.getBody().getStatus());
        assertEquals("Account Disabled", responseEntity.getBody().getError());
        assertEquals("Account has been disabled", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleServiceUnavailableException() {
        ServiceUnavailableException ex = new ServiceUnavailableException("External service is down");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleServiceUnavailableException(ex,
                request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(503, responseEntity.getBody().getStatus());
        assertEquals("Service Unavailable", responseEntity.getBody().getError());
        assertEquals("External service is down", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleMissingServletRequestPartException() {
        MissingServletRequestPartException ex = new MissingServletRequestPartException("profilePicture");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleMissingServletRequestPart(ex,
                request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("Validation Error", responseEntity.getBody().getError());
        assertEquals("Missing required file part: profilePicture", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "email", "Invalid email format");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleMethodArgumentNotValid(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("Validation Error", responseEntity.getBody().getError());
        assertEquals("email: Invalid email format", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleDataIntegrityViolationExceptionWithUniqueConstraint() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Unique constraint violation: email already exists",
                new RuntimeException("Unique constraint"));
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(409, responseEntity.getBody().getStatus());
        assertEquals("Data Integrity Violation", responseEntity.getBody().getError());
        assertEquals("This record already exists. Please use a unique value.", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleDataIntegrityViolationExceptionWithForeignKeyConstraint() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Foreign key constraint violation: cannot delete",
                new RuntimeException("Foreign key constraint"));
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(409, responseEntity.getBody().getStatus());
        assertEquals("Data Integrity Violation", responseEntity.getBody().getError());
        assertEquals("Cannot perform this operation due to related records in the system.",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleDataIntegrityViolationExceptionWithNotNullConstraint() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "NOT NULL constraint violation: field required",
                new RuntimeException("NOT NULL constraint"));
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(409, responseEntity.getBody().getStatus());
        assertEquals("Data Integrity Violation", responseEntity.getBody().getError());
        assertEquals("One or more required fields are missing.", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleResourceNotFoundExceptionWithNullMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleResourceNotFoundException(ex,
                request);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(404, responseEntity.getBody().getStatus());
        assertEquals("Resource Not Found", responseEntity.getBody().getError());
        assertEquals("The requested resource was not found", responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleKycNotVerifiedExceptionWithNullMessage() {
        KycNotVerifiedException ex = new KycNotVerifiedException(null);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleKycNotVerifiedException(ex,
                request);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(400, responseEntity.getBody().getStatus());
        assertEquals("KYC Verification Required", responseEntity.getBody().getError());
        assertEquals("Your KYC verification is pending or has not been completed. Please complete KYC to proceed.",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleAccountDisabledExceptionWithNullMessage() {
        AccountDisabledException ex = new AccountDisabledException(null);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAccountDisabledException(ex,
                request);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(403, responseEntity.getBody().getStatus());
        assertEquals("Account Disabled", responseEntity.getBody().getError());
        assertEquals("Your account has been disabled. Please contact support for assistance.",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleServiceUnavailableExceptionWithNullMessage() {
        ServiceUnavailableException ex = new ServiceUnavailableException(null);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleServiceUnavailableException(ex,
                request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(503, responseEntity.getBody().getStatus());
        assertEquals("Service Unavailable", responseEntity.getBody().getError());
        assertEquals("The service is temporarily unavailable. Please try again later.",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleUnauthorizedExceptionWithNullMessage() {
        UnauthorizedException ex = new UnauthorizedException(null);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleUnauthorizedException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(401, responseEntity.getBody().getStatus());
        assertEquals("Unauthorized", responseEntity.getBody().getError());
        assertEquals("Authentication credentials are missing or invalid",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }

    @Test
    void handleForbiddenExceptionWithNullMessage() {
        ForbiddenException ex = new ForbiddenException(null);
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleForbiddenException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(403, responseEntity.getBody().getStatus());
        assertEquals("Forbidden", responseEntity.getBody().getError());
        assertEquals("You do not have permission to access this resource",
                responseEntity.getBody().getMessage());
        assertEquals("/api/test", responseEntity.getBody().getPath());
    }
}
