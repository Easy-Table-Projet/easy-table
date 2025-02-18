package org.example.easytable.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class QueueingTestConfig {
    @Bean
    @Qualifier("mockQueue")
    public MockRequestQueue mockRequestQueue() {
        return new MockRequestQueue();
    }
}
