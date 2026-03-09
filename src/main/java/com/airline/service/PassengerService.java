package com.airline.service;

import com.airline.model.Passenger;
import com.airline.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Transactional
    public Passenger resolvePassenger(Passenger candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Passenger details are required");
        }

        if (candidate.getEmail() != null && !candidate.getEmail().isBlank()) {
            var existing = passengerRepository.findByEmail(candidate.getEmail().trim());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        if (candidate.getPassportNumber() != null && !candidate.getPassportNumber().isBlank()) {
            var existing = passengerRepository.findByPassportNumber(candidate.getPassportNumber().trim());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        return passengerRepository.save(candidate);
    }
}

