package org.example.easytable.config;

import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    // 하나의 LettuceConnectionFactory bean 생성 (동기, 비동기 모두 사용 가능)
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, Object> createTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        return setupRedisTemplate(redisTemplate,
                new Jackson2JsonRedisSerializer<>(Object.class),
                lettuceConnectionFactory);
    }

    @Bean
    public ReactiveRedisTemplate<String, ReservationCreateReqDto> createReservationTemplate(
            LettuceConnectionFactory lettuceConnectionFactory
    ) {
        return setupReactiveRedisTemplate(lettuceConnectionFactory,
                new Jackson2JsonRedisSerializer<>(ReservationCreateReqDto.class));
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

    private <T> ReactiveRedisTemplate<String, T> setupReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            Jackson2JsonRedisSerializer<T> jsonSerializer
    ) {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();

        RedisSerializationContext.RedisSerializationContextBuilder<String, T> builder =
                RedisSerializationContext.newSerializationContext(stringSerializer);

        RedisSerializationContext<String, T> context = builder
                .value(jsonSerializer)
                .hashKey(stringSerializer)
                .hashValue(jsonSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
