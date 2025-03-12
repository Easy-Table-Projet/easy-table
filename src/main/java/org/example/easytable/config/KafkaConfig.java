package org.example.easytable.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${stream.consumer.count:50}")
    private int consumerCount;

    // Producer 설정: ReservationCreateReqDto를 JSON 직렬화하여 전송
    @Bean
    public ProducerFactory<String, ReservationCreateReqDto> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ReservationCreateReqDto> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Consumer 설정: JSON 역직렬화를 사용하여 메시지를 읽음
    @Bean
    public ConsumerFactory<String, ReservationCreateReqDto> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "reservation-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new JsonDeserializer<>(ReservationCreateReqDto.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservationCreateReqDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReservationCreateReqDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(consumerCount);
        return factory;
    }

    @Bean
    public NewTopic topic() {
        Map<String, String> configs = new HashMap<>();

        configs.put("retention.bytes", "1073741824"); // 1GB
        configs.put("retention.ms", "86400000"); // 1 day

        return TopicBuilder.name("create-reservation")
            .partitions(3) // 메시지를 저장할 큐 개수 설정
            .replicas(1) // partition 데이터 복제본 개수 설정
            .configs(configs)
            .build();
    }
}
