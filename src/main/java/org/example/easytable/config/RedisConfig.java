package org.example.easytable.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.example.easytable.reservation.dto.request.ReservationGetByRestaurantReqDtoImpl;
import org.example.easytable.reservation.dto.request.ReservationReqDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, ReservationReqDto> redisTemplate() {
        RedisTemplate<String, ReservationReqDto> redisTemplate = new RedisTemplate<>();

        // 커스터마이징된 ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return setupRedisTemplate(redisTemplate, new GenericJackson2JsonRedisSerializer(objectMapper));
    }

    @Bean
    public RedisTemplate<String, ReservationGetByRestaurantReqDtoImpl> redisRestaurantTemplate() {
        RedisTemplate<String, ReservationGetByRestaurantReqDtoImpl> redisTemplate = new RedisTemplate<>();

        // 커스터마이징된 ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return setupRedisTemplate(redisTemplate, new GenericJackson2JsonRedisSerializer(objectMapper));
    }

    private <T> RedisTemplate<String, T> setupRedisTemplate(
            RedisTemplate<String, T> template,
            GenericJackson2JsonRedisSerializer serializer
    ) {
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setDefaultSerializer(serializer);

        return template;
    }
}
