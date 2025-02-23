package org.example.easytable.reservation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReservationGetByMemberReqDto {
    private Long memberId;
}
