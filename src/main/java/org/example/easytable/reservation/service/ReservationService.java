package org.example.easytable.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.easytable.common.aop.annotation.RedissonLock;
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
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final RedissonClient redissonClient;

    @Transactional
    public ReservationCreateResDto save(Long restaurantId, LocalDateTime reservationTime, int guestCount) {

        Restaurant foundRestaurant = findRestaurantWithLock(restaurantId);

        Reservation createdReservation = Reservation.builder()
                .member(new Member()) //member 코드 추가시에 member 지정 가능
                .restaurant(foundRestaurant)
                .reservationTime(reservationTime)
                .status(ReservationStatus.CONFIRMED)
                .isDeleted(false)
                .build();

        saveReservationWithLock(foundRestaurant, createdReservation, guestCount);

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
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다"));

        if (!foundReservation.getRestaurant().getId().equals(restaurantId)) {
            throw CustomException.of(ErrorCode.BAD_REQUEST, "이 예약은 해당 식당에 속하지 않습니다.");
        }

        Restaurant foundRestaurant = findRestaurantWithLock(restaurantId);
        deleteReservationWithLock(foundRestaurant, foundReservation);
    }

    @RedissonLock(key = "'lock:restaurant:' + #restaurant.getId()", readOnly = true)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Restaurant findRestaurantWithLock(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));
    }

    @RedissonLock(key = "'lock:restaurant:' + #restaurant.getId()")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveReservationWithLock(Restaurant restaurant, Reservation reservation, int guestCount) {
        reservationRepository.save(reservation);
        restaurant.changeValidSeatCount(-guestCount);
        restaurantRepository.save(restaurant);
    }

    @RedissonLock(key = "'lock:restaurant:' + #restaurant.getId()")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteReservationWithLock(Restaurant restaurant, Reservation reservation) {
        // 비즈니스 로직 수행
        reservationRepository.delete(reservation);
        // TODO: reservation에 예약자 수 추가할 것
        // TODO: reservation에서 예약 인원 가져와 restaurant의 ValidSeatCount에 반영하도록 구현할 것
    }
}
