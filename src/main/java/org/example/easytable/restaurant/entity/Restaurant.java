package org.example.easytable.restaurant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.easytable.common.entity.BaseEntity;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.reservation.entity.Reservation;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "restaurant",
        indexes = {
                @Index(name = "idx_category", columnList = "category"),
                @Index(name = "idx_is_deleted", columnList = "isDeleted")
        })
public class Restaurant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private int maxTableCount;

    @Column(nullable = false)
    private int remainingTableCount;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantCategory category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    @Builder
    public Restaurant(String name, String address, int maxTableCount, RestaurantCategory category, Member owner) {
        this.name = name;
        this.address = address;
        this.maxTableCount = maxTableCount;
        this.remainingTableCount = maxTableCount;
        this.category = category;
        this.owner = owner;
    }


    public void softDelete() {
        this.isDeleted = true;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void decreaseRemainingTableCount() {
        if (this.remainingTableCount <= 0) {
            throw CustomException.of(ErrorCode.BAD_REQUEST, "사용 가능한 테이블 수가 부족합니다.");
        }
        this.remainingTableCount--;
    }
}