package org.example.easytable.reservation.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.entity.ReservationStatus;

@RequiredArgsConstructor
@Getter
public class ReservationGetRes {

    private final Long member_id;
    private final Long restaurant_id;
    private final LocalDateTime reservation_time;
    private final ReservationStatus status;


}

