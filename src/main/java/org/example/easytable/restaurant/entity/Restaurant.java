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

    private boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    private RestaurantCategory restaurantCategory;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    @Builder
    public Restaurant(String name, String address, int validSeatCount, RestaurantCategory restaurantCategory) {
        this.name = name;
        this.address = address;
        this.validSeatCount = validSeatCount;
        this.restaurantCategory = restaurantCategory;
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
