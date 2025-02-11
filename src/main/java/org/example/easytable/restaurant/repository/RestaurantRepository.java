package org.example.easytable.restaurant.repository;

import org.example.easytable.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("""
            SELECT r
            FROM Restaurant r
            WHERE (:restaurantName IS NULL OR r.name LIKE %:restaurantName%)
            AND r.isDeleted IS FALSE 
            """)
    Page<Restaurant> findAllRestaurantByTitle(String restaurantName, Pageable pageable);
}
