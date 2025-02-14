package org.example.easytable.restaurant.repository;

import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query(value = """
        SELECT * FROM restaurant r
        WHERE (:restaurantName IS NULL OR MATCH(r.name) AGAINST(:restaurantName IN NATURAL LANGUAGE MODE))
        AND (:category IS NULL OR r.restaurant_category = :category)
        AND r.is_deleted = FALSE
        """, nativeQuery = true)
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
    List<Restaurant> findTop100RestaurantList(
            @Param("oneMonthAgo") LocalDateTime oneMonthAgo);
}
