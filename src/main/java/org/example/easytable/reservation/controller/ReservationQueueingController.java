package org.example.easytable.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.legacy.ReservationServiceV2;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v2/reservations")
@RequiredArgsConstructor
public class ReservationQueueingController {
    private final ReservationServiceV2 reservationService;

    @PostMapping("/{restaurantId}")
    public Mono<ReservationCreateResDto> createReservation(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestBody ReservationPostReqDto requestDto

    ) {
        Long memberId = AuthUtil.getId();

        return reservationService.submitReservation(new ReservationCreateReqDto(
                restaurantId, memberId, requestDto
        ));
    }
}
