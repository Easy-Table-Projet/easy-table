package org.example.easytable.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

        saveReservationWithLock(createdReservation, foundRestaurant, guestCount);

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
        deleteReservationWithLock(foundReservation, foundRestaurant);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Restaurant findRestaurantWithLock(Long restaurantId) {
        // TODO: 설정값, 의존성 주입되도록 리팩토링할 것
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redissonClient = Redisson.create(config);

        RReadWriteLock rwLock = redissonClient.getReadWriteLock(String.format("lock:restaurant:%d", restaurantId));
        RLock readLock = rwLock.readLock();
        // TODO: lock 점유 시간도 설정 파일로 옮길 것
        readLock.lock(2000, TimeUnit.MILLISECONDS);

        try {
            // 트랜잭션 동기화에 등록: 트랜잭션 종료(커밋 또는 롤백) 시점에 락 해제
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // 트랜잭션 종료 후 락 해제
                    if (readLock.isHeldByCurrentThread()) {
                        readLock.unlock();
                    }
                }
            });

            // 비즈니스 로직 수행
            return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        } catch (Exception e) {
            // 만약 락 등록 전에 예외가 발생하는 경우, 안전하게 락 해제
            if (readLock.isHeldByCurrentThread()) {
                readLock.unlock();
            }
            redissonClient.shutdown();
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveReservationWithLock(Reservation reservation, Restaurant restaurant, int guestCount) {
        // TODO: 중복되는 코드 AOP 등의 방식으로 분리할 것
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redissonClient = Redisson.create(config);

        RReadWriteLock rwLock = redissonClient.getReadWriteLock(String.format("lock:restaurant:%d", restaurant.getId()));
        RLock writeLock = rwLock.writeLock();
        writeLock.lock(2000, TimeUnit.MILLISECONDS);

        try {
            // 트랜잭션 동기화에 등록: 트랜잭션 종료(커밋 또는 롤백) 시점에 락 해제
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // 트랜잭션 종료 후 락 해제
                    if (writeLock.isHeldByCurrentThread()) {
                        writeLock.unlock();
                    }
                }
            });

            // 비즈니스 로직 수행
            reservationRepository.save(reservation);
            restaurant.changeValidSeatCount(-guestCount);
            restaurantRepository.save(restaurant);

        } catch (Exception e) {
            // 만약 락 등록 전에 예외가 발생하는 경우, 안전하게 락 해제
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
            redissonClient.shutdown();
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteReservationWithLock(Reservation reservation, Restaurant restaurant) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redissonClient = Redisson.create(config);

        RReadWriteLock rwLock = redissonClient.getReadWriteLock(String.format("lock:restaurant:%d", restaurant.getId()));
        RLock writeLock = rwLock.writeLock();
        writeLock.lock(2000, TimeUnit.MILLISECONDS);


        // 트랜잭션 종료(커밋 또는 롤백) 시점에 락 해제
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // 트랜잭션 종료 후 락 해제
                if (writeLock.isHeldByCurrentThread()) {
                    writeLock.unlock();
                }
            }
        });

        // 비즈니스 로직 수행
        reservationRepository.delete(reservation);
            // TODO: reservation에서 예약 인원 가져와 restaurant의 ValidSeatCount에 반영하도록 구현할 것
    }
}
