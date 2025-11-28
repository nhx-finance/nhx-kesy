package com.javaguy.nhx.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    void convertToLocalDateTimeViaInstant_Success() {
        Date date = new Date();
        LocalDateTime localDateTime = DateUtil.convertToLocalDateTimeViaInstant(date);

        assertNotNull(localDateTime);
        // Verify conversion by converting back and comparing timestamps
        Date convertedBackDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        assertEquals(date.getTime(), convertedBackDate.getTime());
    }

    @Test
    void convertToLocalDateTimeViaInstant_NullInput_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DateUtil.convertToLocalDateTimeViaInstant(null));
    }

    @Test
    void convertToDateViaInstant_Success() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = DateUtil.convertToDateViaInstant(localDateTime);

        assertNotNull(date);
        // Verify conversion by converting back and comparing timestamps
        // Note: Date only has millisecond precision, so nanoseconds will be lost
        LocalDateTime convertedBackLocalDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(localDateTime.withNano(convertedBackLocalDateTime.getNano()), convertedBackLocalDateTime);
    }

    @Test
    void convertToDateViaInstant_NullInput_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DateUtil.convertToDateViaInstant(null));
    }
}
