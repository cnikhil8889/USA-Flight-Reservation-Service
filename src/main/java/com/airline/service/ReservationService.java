package com.airline.service;

import com.airline.api.error.ResourceNotFoundException;
import com.airline.model.Baggage;
import com.airline.model.BaggageType;
import com.airline.model.Flight;
import com.airline.model.Passenger;
import com.airline.model.Reservation;
import com.airline.model.ReservationStatus;
import com.airline.service.events.ReservationEventPublisher;
import com.airline.service.notifications.NotificationPublisher;
import com.airline.repository.ReservationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final FlightService flightService;

    private final BaggageService baggageService;

    private final PassengerService passengerService;

    private final SeatInventoryService seatInventoryService;

    private final ReservationCodeGenerator reservationCodeGenerator;

    private final MeterRegistry meterRegistry;

    private final ReservationEventPublisher reservationEventPublisher;

    private final NotificationPublisher notificationPublisher;

    @Autowired
    public ReservationService(
            ReservationRepository reservationRepository,
            FlightService flightService,
            BaggageService baggageService,
            PassengerService passengerService,
            SeatInventoryService seatInventoryService,
            ReservationCodeGenerator reservationCodeGenerator,
            MeterRegistry meterRegistry,
            ReservationEventPublisher reservationEventPublisher,
            NotificationPublisher notificationPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.flightService = flightService;
        this.baggageService = baggageService;
        this.passengerService = passengerService;
        this.seatInventoryService = seatInventoryService;
        this.reservationCodeGenerator = reservationCodeGenerator;
        this.meterRegistry = meterRegistry;
        this.reservationEventPublisher = reservationEventPublisher;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional
    public Reservation createReservation(Long flightId, Passenger passenger,
                                         Double baggageWeight, BaggageType baggageType) {
        // Fetch flight
        Flight flight = flightService.getFlightById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + flightId));

        Passenger resolvedPassenger = passengerService.resolvePassenger(passenger);

        // Reserve seat (transactional)
        seatInventoryService.reserveOneSeat(flightId);

        // Start with flight base price
        BigDecimal totalAmount = flight.getPrice();

        // Calculate baggage charge if baggageWeight > 0
        if (baggageWeight != null && baggageWeight > 0) {
            Baggage baggage = baggageService.calculateBaggageCharge(
                    baggageWeight,
                    baggageType != null ? baggageType : BaggageType.CARRY_ON
            );
            totalAmount = totalAmount.add(baggage.getCharge());
        }

        // Create reservation including baggage info
        Reservation reservation = new Reservation(flight, resolvedPassenger, totalAmount,
                baggageWeight != null ? baggageWeight : 0.0,
                baggageType != null ? baggageType : BaggageType.CARRY_ON
        );
        assignReservationCodeWithRetry(reservation);
        Reservation saved = reservationRepository.save(reservation);
        meterRegistry.counter("reservations.created.total").increment();

        reservationEventPublisher.publishReservationCreated(saved);
        notificationPublisher.sendReservationConfirmation(resolvedPassenger.getEmail(), saved);

        return saved;
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public Optional<Reservation> getReservationByCode(String code) {
        return reservationRepository.findByReservationCode(code);
    }

    public List<Reservation> getReservationsByEmail(String email) {
        return reservationRepository.findByPassengerEmail(email);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public boolean cancelReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservation -> {
            reservation.setStatus(ReservationStatus.CANCELLED);
            Reservation saved = reservationRepository.save(reservation);
            seatInventoryService.releaseOneSeat(reservation.getFlight().getId());

            reservationEventPublisher.publishReservationCancelled(saved);
            notificationPublisher.sendReservationCancellation(saved.getPassenger().getEmail(), saved);

            return true;
        }).orElse(false);
    }

    private void assignReservationCodeWithRetry(Reservation reservation) {
        int attempts = 0;
        while (attempts < 5) {
            reservation.setReservationCode(reservationCodeGenerator.generateCode());
            try {
                // Probe uniqueness by attempting a dry save via repository exists lookup
                if (reservationRepository.findByReservationCode(reservation.getReservationCode()).isEmpty()) {
                    return;
                }
            } catch (DataIntegrityViolationException ignored) {
                // fall through and retry with a new code
            }
            attempts++;
        }
        // last resort: let DB enforce uniqueness and surface conflict
        reservation.setReservationCode(reservationCodeGenerator.generateCode());
    }
}