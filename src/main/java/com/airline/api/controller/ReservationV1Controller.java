package com.airline.api.controller;

import com.airline.api.dto.CreateReservationRequest;
import com.airline.api.dto.FlightSummaryResponse;
import com.airline.api.dto.PassengerInput;
import com.airline.api.dto.PassengerResponse;
import com.airline.api.dto.ReservationResponse;
import com.airline.model.Passenger;
import com.airline.model.Reservation;
import com.airline.model.ReservationStatus;
import com.airline.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationV1Controller {

    private final ReservationService reservationService;

    public ReservationV1Controller(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest req) {
        Passenger passenger = toPassengerEntity(req.passenger());
        Reservation reservation = reservationService.createReservation(
                req.flightId(),
                passenger,
                req.baggageWeight(),
                req.baggageType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reservation));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable Long reservationId) {
        return reservationService.getReservationById(reservationId)
                .map(ReservationV1Controller::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<ReservationResponse> getByCode(@PathVariable String code) {
        return reservationService.getReservationByCode(code)
                .map(ReservationV1Controller::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long reservationId) {
        boolean cancelled = reservationService.cancelReservation(reservationId);
        return cancelled ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> listReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String email
    ) {
        List<Reservation> base = (email != null && !email.isBlank())
                ? reservationService.getReservationsByEmail(email.trim())
                : reservationService.getAllReservations();

        List<ReservationResponse> result = base.stream()
                .filter(r -> status == null || r.getStatus() == status)
                .map(ReservationV1Controller::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    private static Passenger toPassengerEntity(PassengerInput input) {
        return new Passenger(
                input.firstName(),
                input.lastName(),
                input.email(),
                input.phoneNumber(),
                input.passportNumber()
        );
    }

    private static ReservationResponse toResponse(Reservation r) {
        var f = r.getFlight();
        var p = r.getPassenger();

        FlightSummaryResponse flight = new FlightSummaryResponse(
                f.getId(),
                f.getFlightNumber(),
                f.getOrigin(),
                f.getDestination(),
                f.getDepartureTime(),
                f.getArrivalTime(),
                f.getPrice(),
                f.getAvailableSeats()
        );

        PassengerResponse passenger = new PassengerResponse(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getEmail(),
                p.getPhoneNumber(),
                p.getPassportNumber()
        );

        return new ReservationResponse(
                r.getId(),
                r.getReservationCode(),
                r.getStatus(),
                r.getBookingDate(),
                r.getTotalAmount(),
                flight,
                passenger,
                r.getBaggageWeight(),
                r.getBaggageType()
        );
    }
}

