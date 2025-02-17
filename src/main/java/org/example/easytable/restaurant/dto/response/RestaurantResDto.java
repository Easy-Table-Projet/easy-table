package org.example.easytable.restaurant.dto.response;

import lombok.Builder;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;

@Builder
public record RestaurantResDto(
        Long id,
        String name,
        String address,
        int maxTableCount,
        int remainingTableCount,
        RestaurantCategory category,
        Long ownerId
) {
    public static RestaurantResDto from(Restaurant restaurant) {
        return RestaurantResDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .maxTableCount(restaurant.getMaxTableCount())
                .remainingTableCount(restaurant.getRemainingTableCount())
                .category(restaurant.getCategory())
                .ownerId(restaurant.getOwner().getId())
                .build();
    }
}
