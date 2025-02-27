package org.example.easytable.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.queueing.ReservationServiceV3;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/reservations")
@RequiredArgsConstructor
public class ReservationQueueingControllerV3 {
    private final ReservationServiceV3 reservationService;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<ReservationCreateResDto> createReservation(
            @PathVariable Long restaurantId,
            @RequestBody ReservationPostReqDto requestDto

    ) throws Exception {
        Long memberId = AuthUtil.getId();

        return ResponseEntity.ok(reservationService.queueRequest(new ReservationCreateReqDto(
                restaurantId, memberId, requestDto
        )));
    }
}
