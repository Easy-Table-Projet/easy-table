package org.example.easytable.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.easytable.member.entity.Member;
import org.example.easytable.reservation.dto.response.ReservationCreateRes;
import org.example.easytable.reservation.dto.response.ReservationGetRes;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationCreateRes save(Long restaurantId,
            LocalDateTime reservationTime) {
        //TODO : RestaurantId로 식당 조회 및 예외처리

        Reservation createdReservation = Reservation.builder()
                .member(new Member())
                .restaurant(new Restaurant())
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


    public List<ReservationGetRes> getReservation() {

        List<Reservation> reservationList = reservationRepository.findAll();

        return reservationList.stream().map(reservation -> new ReservationGetRes(
                        reservation.getMember().getId(),
                        reservation.getRestaurant().getId(),
                        reservation.getReservationTime(),
                        reservation.getStatus()))
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteReservation(Long restaurantId, Long reservationId) {
        Reservation foundReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 예약입니다"));

        if (!foundReservation.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("이 예약은 해당 식당에 속하지 않습니다.");
        }
        reservationRepository.delete(foundReservation);
    }
}
