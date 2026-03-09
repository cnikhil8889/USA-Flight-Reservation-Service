package com.airline.service.notifications;

import com.airline.model.Reservation;

public interface NotificationPublisher {

    void sendReservationConfirmation(String email, Reservation reservation);

    void sendReservationCancellation(String email, Reservation reservation);
}

