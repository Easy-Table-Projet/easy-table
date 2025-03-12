package org.example.easytable.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

@Configuration
public class RedisConfig {
    private static final String TOPIC_NAME = "restaurant:stream";

    @Value("${spring.data.redis.host:localhost}")
    private String host;
    @Value("${spring.data.redis.port:6379}")
    private int port;
    @Value("${redis.streams.consumer_group.size:50}")
    private int consumerGroupSize;
    @Value("${redis.streams.consumer_group.poll-out-ms:10}")
    private int pollOutMillis;
    @Value("${spring.data.redis.password:}")  // ✅ 기본값 유지 (비밀번호 없을 경우 빈 문자열)
    private String password;
    @Value("${spring.data.redis.username:default}")
    private String username;

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic(TOPIC_NAME);
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);

        if (password != null && !password.trim().isEmpty()) {
            config.setPassword(password);
        }

        if (username != null && !username.trim().isEmpty()) {
            config.setUsername(username);
        }

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> createTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        return setupRedisTemplate(redisTemplate,
                new Jackson2JsonRedisSerializer<>(Object.class),
                lettuceConnectionFactory);
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory
    ) {
        Executor executor = Executors.newFixedThreadPool(consumerGroupSize); // Consumer Thread 수 조정

        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(consumerGroupSize)
                        .errorHandler(t -> System.err.println("Stream 소비 중 에러 발생: " + t.getMessage()))
                        .pollTimeout(Duration.ofMillis(pollOutMillis))
                        .executor(executor)
                        .build();

        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }

    private <T> RedisTemplate<String, T> setupRedisTemplate(
            RedisTemplate<String, T> template,
            Jackson2JsonRedisSerializer<T> serializer,
            RedisConnectionFactory connectionFactory
    ) {
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setDefaultSerializer(serializer);

        return template;
    }
}
