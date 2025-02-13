package org.example.easytable.restaurant.dto.request;

public record RestaurantCreateDto(
        String name,
        String address,
        String category
) {
}
