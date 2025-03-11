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
import java.util.Optional;

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

    // 비관적 lock을 적용하려는 경우에 어노테이션을 활성화시킬 것
    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Restaurant> findFirstById(Long id);
}
