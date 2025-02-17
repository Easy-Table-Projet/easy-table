package org.example.easytable.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")  // ✅ 기본값 유지 (localhost)
    private String host;

    @Value("${spring.data.redis.port:6379}")  // ✅ 기본값 유지 (6379)
    private int port;

    @Value("${spring.data.redis.password:}")  // ✅ 기본값 유지 (비밀번호 없을 경우 빈 문자열)
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(String.format("redis://%s:%d", host, port));

        // 🔥 비밀번호가 설정된 경우에만 인증 적용 (기본값 유지)
        if (password != null && !password.trim().isEmpty()) {
            config.useSingleServer().setPassword(password);
        }

        return Redisson.create(config);
    }
}
