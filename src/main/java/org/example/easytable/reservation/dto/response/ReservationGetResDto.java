package org.example.easytable.reservation.dto.response;

import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationGetResDto(
        Long memberId,
        Long restaurantId,
        LocalDateTime reservationTime,
        ReservationStatus status
) {
    public static ReservationGetResDto from(Reservation reservation) {
        return new ReservationGetResDto(
                reservation.getMember().getId(),
                reservation.getRestaurant().getId(),
                reservation.getReservationTime(),
                reservation.getStatus()
        );
    }
}
