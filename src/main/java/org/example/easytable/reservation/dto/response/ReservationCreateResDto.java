package org.example.easytable.reservation.dto.response;

import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationCreateResDto(
        Long requestId,
        Long reservationId,
        Long memberId,
        Long restaurantId,
        LocalDateTime reservationTime,
        ReservationStatus status
) {
    public static ReservationCreateResDto from(Reservation reservation) {
        return new ReservationCreateResDto(
                System.currentTimeMillis(),
                reservation.getId(),
                reservation.getMember().getId(),
                reservation.getRestaurant().getId(),
                reservation.getReservationTime(),
                reservation.getStatus()
        );
    }

    public static ReservationCreateResDto of(Reservation reservation, Long requestId) {
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