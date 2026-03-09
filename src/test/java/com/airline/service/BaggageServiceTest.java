package com.airline.service;

import com.airline.config.BaggagePricingProperties;
import com.airline.model.Baggage;
import com.airline.model.BaggageType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaggageServiceTest {

    @Test
    void carryOnWithinLimitIsFree() {
        BaggagePricingProperties props = new BaggagePricingProperties();
        BaggageService service = new BaggageService(props);

        Baggage baggage = service.calculateBaggageCharge(5.0, BaggageType.CARRY_ON);

        assertEquals(BigDecimal.ZERO.setScale(2), baggage.getCharge());
        assertEquals(BaggageType.CARRY_ON, baggage.getType());
    }

    @Test
    void checkedWithinLimitUsesBaseFee() {
        BaggagePricingProperties props = new BaggagePricingProperties();
        props.setCheckedBaseFee(new BigDecimal("400.00"));
        BaggageService service = new BaggageService(props);

        Baggage baggage = service.calculateBaggageCharge(20.0, BaggageType.CHECKED);

        assertEquals(new BigDecimal("400.00"), baggage.getCharge());
        assertEquals(BaggageType.CHECKED, baggage.getType());
    }

    @Test
    void overweightAddsPerKgFee() {
        BaggagePricingProperties props = new BaggagePricingProperties();
        props.setCheckedBaseFee(new BigDecimal("400.00"));
        props.setOverweightPerKg(new BigDecimal("300.00"));
        BaggageService service = new BaggageService(props);

        // 25kg -> 2kg over 23kg
        Baggage baggage = service.calculateBaggageCharge(25.0, BaggageType.CHECKED);

        assertEquals(new BigDecimal("1000.00"), baggage.getCharge()); // 400 + 2*300
        assertEquals(BaggageType.OVERWEIGHT, baggage.getType());
    }
}

