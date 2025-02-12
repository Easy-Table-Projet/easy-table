package org.example.easytable.restaurant.dto.response;

import lombok.Builder;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;

@Builder
public record RestaurantResDto(
        Long id,
        String name,
        String address,
        RestaurantCategory category
) {
    public static RestaurantResDto from(Restaurant restaurant){
        return RestaurantResDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .category(restaurant.getRestaurantCategory())
                .build();
    }
}
