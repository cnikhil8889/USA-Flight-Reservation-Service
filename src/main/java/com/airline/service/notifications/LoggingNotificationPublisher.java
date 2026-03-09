package com.airline.service.notifications;

import com.airline.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple logging-based implementation useful for local development.
 * In AWS environments this can be replaced with an SES-backed publisher.
 */
@Component
public class LoggingNotificationPublisher implements NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationPublisher.class);

    @Override
    public void sendReservationConfirmation(String email, Reservation reservation) {
        log.info("Reservation confirmation email (simulated) to {} for reservationCode={}", email, reservation.getReservationCode());
    }

    @Override
    public void sendReservationCancellation(String email, Reservation reservation) {
        log.info("Reservation cancellation email (simulated) to {} for reservationCode={}", email, reservation.getReservationCode());
    }
}

