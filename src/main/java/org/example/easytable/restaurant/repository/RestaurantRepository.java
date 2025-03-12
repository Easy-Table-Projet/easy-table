package org.example.easytable.restaurant.repository;

import org.example.easytable.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query(value = """
        SELECT * FROM restaurant r
        WHERE (:name IS NULL OR r.name LIKE %:name%)
        AND (:category IS NULL OR r.category = :category)
        AND r.is_deleted = FALSE
        """, nativeQuery = true)
    Page<Restaurant> findAllRestaurantByTitleAndCategory(
            @Param("name") String name,
            @Param("category") String category,
            Pageable pageable);

    @Query("""
    SELECT r FROM Restaurant r
    LEFT JOIN r.reservations res
    WHERE res.reservationTime >= :oneMonthAgo
    GROUP BY r.id, r.address, r.createdAt, r.isDeleted, r.name, r.category, r.updatedAt
    ORDER BY COUNT(res.id) DESC
""")
    List<Restaurant> findTop100RestaurantList(
            @Param("oneMonthAgo") LocalDateTime oneMonthAgo);

    @Modifying
    @Query(
            "UPDATE Restaurant r " +
            "SET r.remainingTableCount = r.remainingTableCount - 1 " +
            "WHERE r.id = :restaurantId AND r.remainingTableCount > 0"
    )
    int decreaseRemainingTableCount(@Param("restaurantId") Long restaurantId);
}
