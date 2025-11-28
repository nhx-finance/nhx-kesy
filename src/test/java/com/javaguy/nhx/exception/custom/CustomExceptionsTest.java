package com.javaguy.nhx.exception.custom;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

public class CustomExceptionsTest {

    private static final String TEST_MESSAGE = "Test exception message";

    @Test
    void testInvalidOtpException() {
        InvalidOtpException exception = new InvalidOtpException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testInvalidCredentialsException() {
        InvalidCredentialsException exception = new InvalidCredentialsException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void testEmailAlreadyExistsException() {
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testUserNotFoundException() {
        UserNotFoundException exception = new UserNotFoundException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testInvalidTokenException() {
        InvalidTokenException exception = new InvalidTokenException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void testTokenExpiredException() {
        TokenExpiredException exception = new TokenExpiredException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void testInvalidDocumentException() {
        InvalidDocumentException exception = new InvalidDocumentException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testInvalidAgeException() {
        InvalidAgeException exception = new InvalidAgeException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testOtpDeliveryException() {
        OtpDeliveryException exception = new OtpDeliveryException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    }

    @Test
    void testKycAlreadyVerifiedException() {
        KycAlreadyVerifiedException exception = new KycAlreadyVerifiedException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testWalletAlreadyWhitelistedException() {
        WalletAlreadyWhitelistedException exception = new WalletAlreadyWhitelistedException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testInsufficientAmountException() {
        InsufficientAmountException exception = new InsufficientAmountException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testInvalidMintAmountException() {
        InvalidMintAmountException exception = new InvalidMintAmountException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testPaymentFailedException() {
        PaymentFailedException exception = new PaymentFailedException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testStorageException() {
        StorageException exception = new StorageException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void testEmailServiceException() {
        EmailServiceException exception = new EmailServiceException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void testInternalServerException() {
        InternalServerException exception = new InternalServerException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    @Test
    void testAccountDisabledException() {
        AccountDisabledException exception = new AccountDisabledException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void testKycNotVerifiedException() {
        KycNotVerifiedException exception = new KycNotVerifiedException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void testUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    @Test
    void testValidationException() {
        ValidationException exception = new ValidationException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    @Test
    void testForbiddenException() {
        ForbiddenException exception = new ForbiddenException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    @Test
    void testConflictException() {
        ConflictException exception = new ConflictException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    @Test
    void testBadRequestException() {
        BadRequestException exception = new BadRequestException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    @Test
    void testEmailNotVerifiedException() {
        EmailNotVerifiedException exception = new EmailNotVerifiedException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void testServiceUnavailableException() {
        ServiceUnavailableException exception = new ServiceUnavailableException(TEST_MESSAGE);

        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }
}
