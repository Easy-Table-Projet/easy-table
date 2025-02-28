package org.example.easytable.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<ReservationCreateResDto> createReservation(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestBody ReservationPostReqDto requestDto
    ) {
        Long memberId = AuthUtil.getId();

        reservationService.createReservation(restaurantId, memberId, requestDto);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<ReservationGetResDto>> getReservation(
            @PathVariable("restaurantId") Long restaurantId
    ) {

        List<ReservationGetResDto> reservation = reservationService.getReservationByRestaurant(
                restaurantId);

        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<List<ReservationGetResDto>> getReservationByMember() {
        List<ReservationGetResDto> reservation = reservationService.getReservationByMember();

        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }

    @DeleteMapping("/{reservationId}")
    public void deleteReservation(
            @PathVariable("reservationId") Long reservationId
    ) {

        reservationService.deleteReservation(reservationId);

    }
}
