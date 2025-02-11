package org.example.easytable.reservation.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.response.ReservationCreateRes;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
