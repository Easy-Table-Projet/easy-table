package org.example.easytable.reservation.dto.response;

import java.time.LocalDateTime;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;

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
