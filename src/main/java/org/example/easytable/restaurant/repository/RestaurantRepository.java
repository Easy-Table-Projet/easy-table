package org.example.easytable.restaurant.repository;

import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("""
            SELECT r
            FROM Restaurant r
            WHERE (:restaurantName IS NULL OR r.name LIKE %:restaurantName%)
            AND (:category IS NULL OR r.category = :category)
            AND r.isDeleted IS FALSE 
            """)
    Page<Restaurant> findAllRestaurantByTitleAndCategory(
            @Param("restaurantName") String restaurantName,
            @Param("category") RestaurantCategory enumCategory,
            Pageable pageable);

    // 비관적 lock을 적용하려는 경우에 어노테이션을 활성화시킬 것
    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Restaurant> findFirstById(Long id);
}
