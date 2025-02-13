package org.example.easytable.restaurant.repository;

import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("""
            SELECT r
            FROM Restaurant r
            WHERE (:restaurantName IS NULL OR r.name LIKE %:restaurantName%)
            AND (:category IS NULL OR r.restaurantCategory = :category)
            AND r.isDeleted IS FALSE 
            """)
    Page<Restaurant> findAllRestaurantByTitleAndCategory(
            @Param("restaurantName") String restaurantName,
            @Param("category") RestaurantCategory enumCategory,
            Pageable pageable);

    @Query("""
    SELECT r FROM Restaurant r
    LEFT JOIN r.reservations res
    WHERE res.reservationTime >= :oneMonthAgo
    GROUP BY r.id, r.address, r.createdAt, r.isDeleted, r.name, r.restaurantCategory, r.updatedAt
    ORDER BY COUNT(res.id) DESC
""")
    Page<Restaurant> findTop100RestaurantList(
            @Param("oneMonthAgo") LocalDateTime oneMonthAgo,
            Pageable pageable);
}
