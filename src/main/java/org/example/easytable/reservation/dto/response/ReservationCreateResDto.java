package org.example.easytable.reservation.dto.response;

import java.util.UUID;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationCreateResDto(
        String requestId,
        Long reservationId,
        Long memberId,
        Long restaurantId,
        LocalDateTime reservationTime,
        ReservationStatus status
) {
    public static ReservationCreateResDto from(Reservation reservation) {
        return new ReservationCreateResDto(
                UUID.randomUUID().toString(),
                reservation.getId(),
                reservation.getMember().getId(),
                reservation.getRestaurant().getId(),
                reservation.getReservationTime(),
                reservation.getStatus()
        );
    }

    public static ReservationCreateResDto of(Reservation reservation, String requestId) {
        return new ReservationCreateResDto(
                requestId,
                reservation.getId(),
                reservation.getMember().getId(),
                reservation.getRestaurant().getId(),
                reservation.getReservationTime(),
                reservation.getStatus()
        );
    }
}