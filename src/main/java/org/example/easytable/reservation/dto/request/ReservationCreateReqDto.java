package org.example.easytable.reservation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
// TODO: @Data 없애기
@Data
public class ReservationCreateReqDto {
    private final String requestId = UUID.randomUUID().toString();
    private Long restaurantId;
    private Long memberId;
    private ReservationPostReqDto reservationPostReqDto;
}
