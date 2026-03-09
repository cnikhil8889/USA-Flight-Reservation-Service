package com.airline.api.dto;

public record PassengerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String passportNumber
) {}

