package org.example.easytable.restaurant.entity;

import jakarta.persistence.*;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.easytable.common.entity.BaseEntity;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "restaurant",
        indexes = {
                @Index(name = "idx_restaurant_category", columnList = "restaurantCategory"),
                @Index(name = "idx_is_deleted", columnList = "isDeleted")
        })
public class Restaurant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private int validSeatCount;

    private boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private RestaurantCategory restaurantCategory;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    public static Restaurant newRestaurant(RestaurantCreateDto req) {
        return Restaurant.builder()
                .name(req.name())
                .address(req.address())
                .validSeatCount(req.validSeatCount())
                .restaurantCategory(RestaurantCategory.valueOf(req.category()))
                .isDeleted(false)
                .build();
    }

    public void updateRestaurantName(String name) {
        this.name = name;
    }

    public void changeValidSeatCount(int changedSeatCount) {
        this.validSeatCount += changedSeatCount;
    }

    public boolean isReservationAvailable(int reservationHeadCount) {
        return this.validSeatCount >= reservationHeadCount;
    }

    public void deleteRestaurant() {
        this.isDeleted = true;
    }
}
