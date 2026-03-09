package com.airline.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ReservationCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String generateCode() {
        StringBuilder sb = new StringBuilder("AR-");
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }
        return sb.toString();
    }
}

