package org.example.easytable.reservation.dto.request;

import lombok.*;

import java.util.UUID;

// 대기열에 사용되는 메시지 형식
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ReservationCreateReqMessage {
    private final String requestId = UUID.randomUUID().toString();
    private Long restaurantId;
    private Long memberId;
    private ReservationPostReqDto reservationPostReqDto;
}
