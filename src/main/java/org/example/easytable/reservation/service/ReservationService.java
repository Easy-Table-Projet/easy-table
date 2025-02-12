package org.example.easytable.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public ReservationCreateResDto save(Long restaurantId, LocalDateTime reservationTime) {

        Restaurant foundRestaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        Reservation createdReservation = Reservation.builder()
                .member(new Member()) //member 코드 추가시에 member 지정 가능
                .restaurant(foundRestaurant)
                .reservationTime(reservationTime)
                .status(ReservationStatus.CONFIRMED)
                .isDeleted(false)
                .build();

        reservationRepository.save(createdReservation);

        return new ReservationCreateResDto(
                createdReservation.getId(), createdReservation.getMember().getId(),
                createdReservation.getRestaurant().getId(), createdReservation.getReservationTime(),
                createdReservation.getStatus());
    }


    public List<ReservationGetResDto> getReservation() {

        List<Reservation> reservationList = reservationRepository.findAll();

        return reservationList.stream().map(reservation -> new ReservationGetResDto(
                        reservation.getMember().getId(),
                        reservation.getRestaurant().getId(),
                        reservation.getReservationTime(),
                        reservation.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long restaurantId, Long reservationId) {
        Reservation foundReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND,"존재하지 않는 예약입니다"));

        if (!foundReservation.getRestaurant().getId().equals(restaurantId)) {
            throw CustomException.of(ErrorCode.BAD_REQUEST,"이 예약은 해당 식당에 속하지 않습니다.");
        }
        reservationRepository.delete(foundReservation);
    }
}
