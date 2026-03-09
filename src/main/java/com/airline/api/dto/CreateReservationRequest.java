package com.airline.api.dto;

import com.airline.model.BaggageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateReservationRequest(
        @NotNull(message = "Flight ID is required")
        Long flightId,

        @Valid
        @NotNull(message = "Passenger details are required")
        PassengerInput passenger,

        @PositiveOrZero(message = "Baggage weight must be zero or more")
        Double baggageWeight,

        BaggageType baggageType
) {}

