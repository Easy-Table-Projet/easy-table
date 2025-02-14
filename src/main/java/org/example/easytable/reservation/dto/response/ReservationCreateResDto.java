package org.example.easytable.reservation.dto.response;

import java.time.LocalDateTime;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;

public record ReservationCreateResDto(
        Long reservationId,
        Long memberId,
        Long restaurantId,
        LocalDateTime reservationTime,
        ReservationStatus status
) {
    public static ReservationCreateResDto from(Reservation reservation) {
        return new ReservationCreateResDto(
                reservation.getId(),
                reservation.getMember().getId(),
                reservation.getRestaurant().getId(),
                reservation.getReservationTime(),
                reservation.getStatus()
        );
    }
}