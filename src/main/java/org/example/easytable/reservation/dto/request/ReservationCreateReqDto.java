package org.example.easytable.reservation.dto.request;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReservationCreateReqDto {
    private final String requestId = UUID.randomUUID().toString();
    private Long restaurantId;
    private Long memberId;
    private ReservationPostReqDto reservationPostReqDto;
}
