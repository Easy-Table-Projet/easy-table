package org.example.easytable.reservation.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeoutException;

/**
 * 1. Kafka에서는 add 시 maxlen을 신경쓰지 않아도 되는가?
 * 2. Kafka에서는 maxlen으로 최대길이가 설정된 스트림에 값을 제대로 전달할 수 없나?
 */

@Repository("kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaMessagePublisherImpl implements MessagePublisher {
    private static final int MAX_ATTEMPTS = 5;
    private static final long RETRY_DELAY_MS = 500;

    private final KafkaTemplate<String, ReservationCreateReqDto> kafkaTemplate;

    @Value("${kafka.topic.reservation:create-reservation}")
    private String topic;

    @Override
    public void publish(ReservationCreateReqDto dto) throws TimeoutException {
        int attempt = 0;
        while (attempt < MAX_ATTEMPTS) {
            try {
                kafkaTemplate.send(topic, dto.getRequestId(), dto).get();
                return;
            } catch (Exception e) {
                attempt++;
                sleepThread();
            }
        }
        throw new TimeoutException("Kafka 메시지 전송 실패");
    }

    private void sleepThread() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            System.out.println("thread interrupted");
        }
    }
}