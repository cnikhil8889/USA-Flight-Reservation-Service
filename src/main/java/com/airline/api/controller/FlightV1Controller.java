package com.airline.api.controller;

import com.airline.api.dto.FlightDetailResponse;
import com.airline.api.dto.FlightSummaryResponse;
import com.airline.model.Flight;
import com.airline.service.FlightService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
public class FlightV1Controller {

    private final FlightService flightService;

    public FlightV1Controller(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<List<FlightSummaryResponse>> listFlights(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureFrom,
            @RequestParam(defaultValue = "true") boolean onlyAvailable
    ) {
        List<Flight> flights = flightService.listFlights(origin, destination, departureFrom, onlyAvailable);
        return ResponseEntity.ok(flights.stream().map(FlightV1Controller::toSummary).toList());
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<FlightDetailResponse> getFlight(@PathVariable Long flightId) {
        return flightService.getFlightById(flightId)
                .map(FlightV1Controller::toDetail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private static FlightSummaryResponse toSummary(Flight f) {
        return new FlightSummaryResponse(
                f.getId(),
                f.getFlightNumber(),
                f.getOrigin(),
                f.getDestination(),
                f.getDepartureTime(),
                f.getArrivalTime(),
                f.getPrice(),
                f.getAvailableSeats()
        );
    }

    private static FlightDetailResponse toDetail(Flight f) {
        return new FlightDetailResponse(
                f.getId(),
                f.getFlightNumber(),
                f.getOrigin(),
                f.getDestination(),
                f.getDepartureTime(),
                f.getArrivalTime(),
                f.getPrice(),
                f.getAvailableSeats(),
                f.getTotalSeats()
        );
    }
}

