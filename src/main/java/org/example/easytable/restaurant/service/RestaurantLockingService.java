package org.example.easytable.restaurant.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    // 컨텍스트 작업을 위해 EntityManager 객체를 직접 사용
    @PersistenceContext
    private EntityManager entityManager;

    @RedissonLock(prefix = "restaurant:")
    @Transactional
    public Restaurant decreaseRemainingTableCountWithLock(@LockKey Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        restaurant.decreaseRemainingTableCount();

        return restaurant;
    }

    @Transactional
    public boolean atomicDecreaseRemainingTableCount(Long restaurantId) {
        int updated = restaurantRepository.decreaseRemainingTableCount(restaurantId);

        // 수동으로 flush()를 호출해 DB와 동기화
        entityManager.flush();

        Restaurant restaurant = entityManager.find(Restaurant.class, restaurantId);
        if (restaurant != null && entityManager.contains(restaurant)) {
            // detach()로 해당 restaurant만 캐싱 해제
            entityManager.detach(restaurant);
        }

        return updated > 0;
    }
}
