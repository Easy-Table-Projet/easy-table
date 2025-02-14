package org.example.easytable.reservation.dto.response;

import java.time.LocalDateTime;
import org.example.easytable.reservation.entity.ReservationStatus;

public record ReservationGetResDto(
        Long memberId,
        Long restaurantId,
        LocalDateTime reservationTime,
        ReservationStatus status
) {}

