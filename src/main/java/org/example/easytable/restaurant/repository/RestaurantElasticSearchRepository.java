package org.example.easytable.restaurant.repository;

import org.example.easytable.restaurant.entity.RestaurantDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RestaurantElasticSearchRepository extends ElasticsearchRepository<RestaurantDocument, Long> {
//    @Query("""
//    {
//      "bool": {
//        "must": [
//          { "match": { "name": "?0" } },
//          { "term": { "category": "?1" } }
//        ],
//        "filter": [
//          { "term": { "isDeleted": false } }
//        ]
//      }
//    }
//    """)
//    Page<RestaurantDocument> searchByFilters(String name, String category, Pageable pageable);
}
