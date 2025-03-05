package org.example.easytable.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.aop.annotation.LockKey;
import org.example.easytable.common.aop.annotation.RedissonLock;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ReservationService의 AOP Self-invocation 문제 해결을 위한 클래스
@Service
@RequiredArgsConstructor
public class RestaurantLockingService {
    private final RestaurantRepository restaurantRepository;

    @RedissonLock(prefix = "restaurant:")
    @Transactional
    public Restaurant decreaseRemainingTableCountWithLock(@LockKey Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        restaurant.decreaseRemainingTableCount();

        return restaurant;
    }

    @Transactional
    public boolean atomicDecreaseRemainingTableCount(@LockKey Long restaurantId) {
        return (restaurantRepository.decreaseRemainingTableCount(restaurantId) > 0);
    }
}
