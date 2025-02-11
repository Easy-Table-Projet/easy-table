package org.example.easytable.reservation.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.response.ReservationCreateRes;
import org.example.easytable.reservation.dto.response.ReservationGetRes;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("{restaurantId}/reservation")
    public ResponseEntity<ReservationCreateRes> save(
            @PathVariable("restaurantId") Long restaurantId,
            LocalDateTime ReservationTime,
            HttpServletRequest request
    ) {
        // TODO :: httpServletRequest 에서 token 안의 member_id 값 추출하기
        reservationService.save(restaurantId, ReservationTime);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{restaurantId}/reservation")
    public ResponseEntity<List<ReservationGetRes>> getReservation(
            @PathVariable("restaurantId") Long restaurantId
    ) {
        //TODO : 특정 식당의 예약 조회, 사용자가 가진 예약 조회
        List<ReservationGetRes> reservation = reservationService.getReservation();

        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }
}
