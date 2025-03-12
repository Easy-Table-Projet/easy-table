package org.example.easytable.reservation.repository;

import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;

import java.util.concurrent.TimeoutException;

public interface MessagePublisher {
    void publish(ReservationCreateReqMessage dto) throws TimeoutException;
}
