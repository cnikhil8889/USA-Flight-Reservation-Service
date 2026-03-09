package com.airline.service;

import com.airline.api.error.ConflictException;
import com.airline.api.error.ResourceNotFoundException;
import com.airline.model.Flight;
import com.airline.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatInventoryService {

    private final FlightRepository flightRepository;

    public SeatInventoryService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional
    public void reserveOneSeat(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + flightId));

        if (flight.getAvailableSeats() == null || flight.getAvailableSeats() <= 0) {
            throw new ConflictException("No seats available for flight: " + flightId);
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);
    }

    @Transactional
    public void releaseOneSeat(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + flightId));

        if (flight.getAvailableSeats() == null) {
            flight.setAvailableSeats(0);
        }

        if (flight.getTotalSeats() == null) {
            return;
        }

        if (flight.getAvailableSeats() < flight.getTotalSeats()) {
            flight.setAvailableSeats(flight.getAvailableSeats() + 1);
            flightRepository.save(flight);
        }
    }
}

