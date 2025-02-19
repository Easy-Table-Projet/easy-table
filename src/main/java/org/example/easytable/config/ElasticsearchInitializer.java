package org.example.easytable.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.entity.RestaurantDocument;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchInitializer {
    private final ElasticsearchTemplate elasticsearchTemplate;

    @PostConstruct
    public void resetElasticsearchIndex() {
        elasticsearchTemplate.indexOps(RestaurantDocument.class).delete();
        elasticsearchTemplate.indexOps(RestaurantDocument.class).create();
        elasticsearchTemplate.indexOps(RestaurantDocument.class).putMapping();
    }
}
