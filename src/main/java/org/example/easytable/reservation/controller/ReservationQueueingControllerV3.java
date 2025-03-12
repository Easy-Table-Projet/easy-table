package org.example.easytable.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.queueing.ReservationQueueingService;
import org.example.easytable.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/reservations")
@RequiredArgsConstructor
public class ReservationQueueingControllerV3 {
    private final ReservationQueueingService reservationService;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<ReservationCreateResDto> createReservation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("restaurantId") Long restaurantId,
            @RequestBody ReservationPostReqDto requestDto

    ) throws Exception {
        Long memberId = userDetails.getId();

        return ResponseEntity.ok(reservationService.publishRequest(new ReservationCreateReqMessage(
                restaurantId, memberId, requestDto
        )));
    }
}
