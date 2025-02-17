package org.example.easytable.restaurant.dto.request;

public record RestaurantCreateReqDto(
        String name,
        String address,
        int maxTableCount,
        String category
) {
}

