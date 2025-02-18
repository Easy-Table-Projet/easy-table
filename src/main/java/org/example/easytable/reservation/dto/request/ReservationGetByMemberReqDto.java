package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.ReservationService;

import java.util.List;

public record ReservationGetByMemberReqDto() implements ReservationReqDto<List<ReservationGetResDto>> {
    @Override
    public void process(ReservationService service) {
        service.getReservationByMember();
    }
}
