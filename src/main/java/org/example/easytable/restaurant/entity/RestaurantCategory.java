package org.example.easytable.restaurant.entity;

import lombok.Getter;

@Getter
public enum RestaurantCategory {
    KOREAN("한식"),
    CHINESE("중식"),
    JAPANESE("일식"),
    WESTERN("양식"),
    ITALIAN("이탈리아음식"),
    FRENCH("프랑스음식"),
    SPANISH("스페인음식"),
    AMERICAN("아메리칸음식"),
    ASIAN("아시아음식"),
    VIETNAMESE("베트남음식"),
    THAI("태국음식"),
    INDIAN("인도음식"),
    FUSION("퓨전음식");

    private final String koreanName;

    RestaurantCategory(String koreanName) {
        this.koreanName = koreanName;
    }
}
