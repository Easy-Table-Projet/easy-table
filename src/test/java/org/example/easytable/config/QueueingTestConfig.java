package org.example.easytable.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class QueueingTestConfig {
    @Bean
    public MockRequestQueue requestQueue() {
        return new MockRequestQueue();
    }
}
