package org.example.easytable.restaurant.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
@Document(indexName = "restaurants")
public class RestaurantDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Integer)
    private int maxTableCount;

    @Field(type = FieldType.Integer)
    private int remainingTableCount;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Long)
    private Long ownerId;

    @Field(type = FieldType.Boolean)
    private boolean isDeleted;

    @Builder
    public static RestaurantDocument from(Restaurant restaurant) {
        return RestaurantDocument.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .maxTableCount(restaurant.getMaxTableCount())
                .remainingTableCount(restaurant.getRemainingTableCount())
                .category(restaurant.getCategory().toString())
                .ownerId(restaurant.getOwner().getId())
                .build();
    }
}