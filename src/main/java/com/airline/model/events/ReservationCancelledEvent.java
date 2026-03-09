package com.airline.model.events;

import com.airline.model.Reservation;

public class ReservationCancelledEvent {

    private final Reservation reservation;

    public ReservationCancelledEvent(Reservation reservation) {
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }
}

