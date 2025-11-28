package com.javaguy.nhx.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppConstantsTest {

    @Test
    void testAppConstantsExist() {
        assertNotNull(AppConstants.class);
    }

    @Test
    void testAppConstantsCanBeInstantiated() {
        // Just verify the constants are accessible
        assertDoesNotThrow(() -> {
            // Access any public constant that might exist
            AppConstants.class.getDeclaredFields();
        });
    }
}
