package org.example.easytable.restaurant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.easytable.common.entity.BaseEntity;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.restaurant.dto.request.CreateRestaurantDto;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private boolean isDeleted;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    public static Restaurant newRestaurant(CreateRestaurantDto req){
        return Restaurant.builder()
                .name(req.name())
                .address(req.address())
                .isDeleted(false)
                .build();
    }
}
