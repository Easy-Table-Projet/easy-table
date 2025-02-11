package org.example.easytable.reservation.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.response.ReservationCreateRes;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationCreateRes save(Long restaurantId,
            LocalDateTime reservationTime) {
        //TODO : RestaurantId로 식당 조회 및 예외처리

        Reservation createdReservation = Reservation.builder()
                .member(null)
                .restaurant(null)
                .reservationTime(reservationTime)
                .status(ReservationStatus.CONFIRMED)
                .isDeleted(false)
                .build();

        reservationRepository.save(createdReservation);

        return new ReservationCreateRes(
                createdReservation.getId(), createdReservation.getMember().getId(),
                createdReservation.getRestaurant().getId(), createdReservation.getReservationTime(),
                createdReservation.getStatus());
    }


}
