package com.airline.model.events;

import com.airline.model.Reservation;

public class ReservationCreatedEvent {

    private final Reservation reservation;

    public ReservationCreatedEvent(Reservation reservation) {
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }
}

