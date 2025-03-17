package org.example.easytable.reservation.repository;

import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;

import java.util.concurrent.TimeoutException;

// 여러 Message queue/event streaming 기술들을 도입해보기 위한 추상화
public interface MessagePublisher {
    void publish(ReservationCreateReqMessage dto) throws TimeoutException;
}
