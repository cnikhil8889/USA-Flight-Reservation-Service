package com.airline.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationCodeGeneratorTest {

    @Test
    void generatesCodeWithPrefixAndLength() {
        ReservationCodeGenerator generator = new ReservationCodeGenerator();
        String code = generator.generateCode();

        assertTrue(code.startsWith("AR-"));
        assertEquals(3 + 8, code.length());
    }

    @Test
    void generatesDifferentCodesMostOfTheTime() {
        ReservationCodeGenerator generator = new ReservationCodeGenerator();
        String first = generator.generateCode();
        String second = generator.generateCode();

        assertNotEquals(first, second);
    }
}

