package com.airline.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlightDetailResponse(
        Long id,
        String flightNumber,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price,
        Integer availableSeats,
        Integer totalSeats
) {}

