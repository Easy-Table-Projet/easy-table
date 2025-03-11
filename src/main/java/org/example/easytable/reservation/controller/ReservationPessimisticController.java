package org.example.easytable.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.aop.annotation.Timer;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationPessimisticLockService;
import org.example.easytable.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pl/reservations")
@RequiredArgsConstructor
public class ReservationPessimisticController {
    private final ReservationPessimisticLockService reservationService;

    @PostMapping("/{restaurantId}")
    @Timer
    public ResponseEntity<ReservationCreateResDto> createReservation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("restaurantId") Long restaurantId,
            @RequestBody ReservationPostReqDto reservationPostDto
    ) {
        Long memberId = userDetails.getId();

        reservationService.createReservation(restaurantId, memberId, reservationPostDto);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
