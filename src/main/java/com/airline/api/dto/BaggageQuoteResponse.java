package com.airline.api.dto;

import com.airline.model.BaggageType;

import java.math.BigDecimal;

public record BaggageQuoteResponse(
        Double weight,
        BaggageType requestedType,
        BaggageType actualType,
        BigDecimal baseFee,
        BigDecimal overweightFee,
        BigDecimal totalFee,
        String description
) {}

