package org.example.easytable.restaurant.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Document(indexName = "restaurants")
@Setting(settingPath = "restaurant-settings.json")
public class RestaurantDocument {
    @Id
    private Long id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "ngram_analyzer"),
                    @InnerField(suffix = "edge_ngram", type = FieldType.Text, analyzer = "edge_ngram_analyzer", searchAnalyzer = "edge_ngram_analyzer"),
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String name;

    @Field(type = FieldType.Text)
    private String address;

    @Field(name = "max_table_count", type = FieldType.Integer)
    private int maxTableCount;

    @Field(name = "remaining_table_count", type = FieldType.Integer)
    private int remainingTableCount;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(name = "is_deleted", type = FieldType.Boolean)
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
                .build();
    }
}