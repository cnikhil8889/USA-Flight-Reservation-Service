package com.airline.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PassengerInput(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Email(message = "Valid email is required")
        @NotBlank(message = "Email is required")
        String email,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Valid phone number is required")
        String phoneNumber,

        @NotBlank(message = "Passport / Govt. ID number is required")
        String passportNumber
) {}

