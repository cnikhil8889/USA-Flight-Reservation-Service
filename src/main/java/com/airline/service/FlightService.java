package com.airline.service;

import com.airline.model.Flight;
import com.airline.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public List<Flight> getAvailableFlights() {
        return flightRepository.findAllAvailableFlights();
    }

    public List<Flight> listFlights(String origin, String destination, LocalDateTime departureFrom, boolean onlyAvailable) {
        return flightRepository.searchFlights(origin, destination, departureFrom, onlyAvailable);
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    public Optional<Flight> getFlightByNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber);
    }

    public List<Flight> searchFlights(String origin, String destination, LocalDateTime departureDate) {
        if (departureDate != null) {
            return flightRepository.findAvailableFlights(origin, destination, departureDate);
        } else {
            return flightRepository.findByOriginAndDestination(origin, destination);
        }
    }

    public Flight saveFlight(Flight flight) {
        return flightRepository.save(flight);
    }
}