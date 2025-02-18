package org.example.easytable.restaurant.repository;

import org.example.easytable.restaurant.entity.RestaurantDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RestaurantElasticSearchRepository extends ElasticsearchRepository<RestaurantDocument, Long> {
}
