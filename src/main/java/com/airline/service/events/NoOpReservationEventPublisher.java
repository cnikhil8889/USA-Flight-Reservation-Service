package com.airline.service.events;

import com.airline.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default implementation used for local development and environments
 * where no external event bus is configured.
 */
@Component
public class NoOpReservationEventPublisher implements ReservationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpReservationEventPublisher.class);

    @Override
    public void publishReservationCreated(Reservation reservation) {
        log.debug("Reservation created (no-op publisher): code={}", reservation.getReservationCode());
    }

    @Override
    public void publishReservationCancelled(Reservation reservation) {
        log.debug("Reservation cancelled (no-op publisher): code={}", reservation.getReservationCode());
    }
}

