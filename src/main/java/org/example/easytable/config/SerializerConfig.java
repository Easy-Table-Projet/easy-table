package org.example.easytable.config;

import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializerConfig {
    @Bean
    public SerializerUtil<ReservationCreateReqDto> serializerUtil() {
        return new SerializerUtil<>(ReservationCreateReqDto.class);
    }
}
