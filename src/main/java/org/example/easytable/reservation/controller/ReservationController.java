package org.example.easytable.reservation.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.easytable.member.entity.Member;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<ReservationCreateResDto> save(
            @PathVariable("restaurantId") Long restaurantId,
            LocalDateTime ReservationTime,
            HttpServletRequest request
    ) {

        // TODO :: httpServletRequest 에서 token 안의 member_id 값 추출하기
        reservationService.save(restaurantId, ReservationTime);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{restaurantId}/reservation")
    public ResponseEntity<List<ReservationGetResDto>> getReservation(
            @PathVariable("restaurantId") Long restaurantId
    ) {
        //TODO : 사용자가 가진 예약 조회, 특정 사용자의 예약 조회(?)
        List<ReservationGetResDto> reservation = reservationService.getReservation();

        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }

    @DeleteMapping("/{restaurantId}/reservation/{reservationId}")
    public void deleteReservation(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("reservationId") Long reservationId
    ) {

        reservationService.deleteReservation(restaurantId, reservationId);

    }
}
