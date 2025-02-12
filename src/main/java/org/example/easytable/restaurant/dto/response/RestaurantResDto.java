package org.example.easytable.restaurant.dto.response;

import lombok.Builder;
import org.example.easytable.restaurant.entity.Restaurant;

@Builder
public record RestaurantResDto(
        Long id,
        String name,
        String address,
        int validSeatCount
) {
    public static RestaurantResDto from(Restaurant restaurant) {
        return RestaurantResDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .validSeatCount(restaurant.getValidSeatCount())
                .build();
    }
}
