package com.airline.api.dto;

import com.airline.model.BaggageType;
import com.airline.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        String code,
        ReservationStatus status,
        LocalDateTime bookedAt,
        BigDecimal totalAmount,
        FlightSummaryResponse flight,
        PassengerResponse passenger,
        Double baggageWeight,
        BaggageType baggageType
) {}

