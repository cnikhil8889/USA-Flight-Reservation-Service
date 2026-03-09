package com.airline.api.dto;

import com.airline.model.BaggageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record BaggageQuoteRequest(
        @NotNull(message = "Weight is required")
        @PositiveOrZero(message = "Weight must be zero or more")
        Double weight,

        @NotNull(message = "Baggage type is required")
        BaggageType requestedType
) {}

