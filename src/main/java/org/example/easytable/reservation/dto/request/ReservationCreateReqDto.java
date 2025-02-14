package org.example.easytable.reservation.dto.request;

import java.time.LocalDateTime;

public record ReservationCreateReqDto(
        LocalDateTime reservationTime,
        int guestCount
) {}
