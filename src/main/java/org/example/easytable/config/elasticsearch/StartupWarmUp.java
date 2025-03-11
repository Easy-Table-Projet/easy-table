package org.example.easytable.config.elasticsearch;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupWarmUp implements CommandLineRunner {
    private final ElasticsearchInitService elasticsearchInitService;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Elasticsearch 워밍업 실행 중...");
        elasticsearchInitService.warmUpElasticsearch();
    }


}
