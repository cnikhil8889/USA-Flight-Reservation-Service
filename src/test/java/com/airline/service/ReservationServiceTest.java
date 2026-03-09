package com.airline.service;

import com.airline.api.error.ResourceNotFoundException;
import com.airline.model.BaggageType;
import com.airline.model.Flight;
import com.airline.model.Passenger;
import com.airline.model.Reservation;
import com.airline.model.ReservationStatus;
import com.airline.repository.ReservationRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private FlightService flightService;
    private BaggageService baggageService;
    private PassengerService passengerService;
    private SeatInventoryService seatInventoryService;
    private ReservationCodeGenerator reservationCodeGenerator;
    private SimpleMeterRegistry meterRegistry;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        flightService = mock(FlightService.class);
        baggageService = mock(BaggageService.class);
        passengerService = mock(PassengerService.class);
        seatInventoryService = mock(SeatInventoryService.class);
        reservationCodeGenerator = mock(ReservationCodeGenerator.class);
        meterRegistry = new SimpleMeterRegistry();

        reservationService = new ReservationService(meterRegistry);
        // inject other collaborators via reflection since fields are autowired in production
        setField(reservationService, "reservationRepository", reservationRepository);
        setField(reservationService, "flightService", flightService);
        setField(reservationService, "baggageService", baggageService);
        setField(reservationService, "passengerService", passengerService);
        setField(reservationService, "seatInventoryService", seatInventoryService);
        setField(reservationService, "reservationCodeGenerator", reservationCodeGenerator);
    }

    @Test
    void createReservation_happyPath_incrementsMetricAndSetsStatus() {
        Long flightId = 1L;
        Flight flight = new Flight();
        flight.setId(flightId);
        flight.setPrice(new BigDecimal("199.00"));
        when(flightService.getFlightById(flightId)).thenReturn(Optional.of(flight));

        Passenger passenger = new Passenger("Alex", "Johnson", "a@example.com", "+1000000000", "P123");
        when(passengerService.resolvePassenger(passenger)).thenReturn(passenger);

        Reservation saved = new Reservation(flight, passenger, new BigDecimal("199.00"), 0.0, BaggageType.CARRY_ON);
        saved.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);
        when(reservationCodeGenerator.generateCode()).thenReturn("AR-TESTCODE");

        Reservation result = reservationService.createReservation(flightId, passenger, 0.0, BaggageType.CARRY_ON);

        assertNotNull(result);
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        assertEquals(1.0, meterRegistry.counter("reservations.created.total").count(), 0.0001);

        verify(seatInventoryService).reserveOneSeat(flightId);
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertEquals("AR-TESTCODE", captor.getValue().getReservationCode());
    }

    @Test
    void createReservation_missingFlight_throwsNotFound() {
        Long flightId = 99L;
        when(flightService.getFlightById(flightId)).thenReturn(Optional.empty());

        Passenger passenger = new Passenger();

        assertThrows(ResourceNotFoundException.class, () ->
                reservationService.createReservation(flightId, passenger, 0.0, BaggageType.CARRY_ON));
    }

    @Test
    void cancelReservation_setsStatusCancelledWhenFound() {
        Long reservationId = 10L;
        Flight flight = new Flight();
        flight.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setFlight(flight);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean cancelled = reservationService.cancelReservation(reservationId);

        assertTrue(cancelled);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        verify(seatInventoryService).releaseOneSeat(flight.getId());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

