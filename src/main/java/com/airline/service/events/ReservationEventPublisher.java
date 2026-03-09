package com.airline.service.events;

import com.airline.model.Reservation;

public interface ReservationEventPublisher {

    void publishReservationCreated(Reservation reservation);

    void publishReservationCancelled(Reservation reservation);
}

