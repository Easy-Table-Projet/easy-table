package org.example.easytable.restaurant.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public Restaurant decreaseRemainingTableCountWithLock(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        restaurant.decreaseRemainingTableCount();

        return restaurant;
    }

    @Transactional
    public Restaurant atomicDecreaseRemainingTableCount(Long restaurantId) {
        int updated = restaurantRepository.updateRemainingTableCount(restaurantId);

        if (updated <= 0) {
            // IllegalArgumentException 대신 CustomException 사용
            throw CustomException.of(ErrorCode.BAD_REQUEST, "현재 여유 테이블이 없습니다.");
        }

        // 수동으로 flush()를 호출해 DB와 동기화
        entityManager.flush();

        Restaurant restaurant = entityManager.find(Restaurant.class, restaurantId);
        if (restaurant == null) { throw CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"); }

        if (entityManager.contains(restaurant)) {
            // detach()로 해당 restaurant만 캐싱 해제
            entityManager.detach(restaurant);
        }

        return restaurant;
    }
}
