package org.example.easytable.reservation.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;

@Builder
public record ReservationCreateResDto(
        Long reservationId,
        Long memberId,
        Long restaurantId,
        LocalDateTime reservationTime,
        ReservationStatus status
) {
    public static ReservationCreateResDto from(Reservation reservation){
        return ReservationCreateResDto.builder()
                .reservationId(reservation.getId())
                .memberId(reservation.getMember().getId())
                .restaurantId(reservation.getRestaurant().getId())
                .reservationTime(reservation.getReservationTime())
                .status(reservation.getStatus())
                .build();

    }
}
