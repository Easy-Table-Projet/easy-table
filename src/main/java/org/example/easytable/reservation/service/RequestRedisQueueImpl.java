package org.example.easytable.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.reservation.dto.request.ReservationReqDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
@Qualifier("redisQueue")
@RequiredArgsConstructor
@Slf4j
public class RequestRedisQueueImpl implements RequestQueue {
    private static final String QUEUE_KEY = "reservation:request:queue";

    private final RedisTemplate<String, ReservationReqDto> redisTemplate;
    private final RequestFutureStore requestFutureStore;
    private final ReservationService service;

    @Value("${queue-capacity:25}")
    private int capacity;

    @Override
    public boolean enqueue(ReservationReqDto request) {
        Long result = redisTemplate.opsForSet().add(QUEUE_KEY, request);
        log.debug("queued Request: {}", request);
        return result != null;
    }

    @Override
    // @Scheduled(fixedDelay = 10000)
    public synchronized void processQueue() {
        ReservationReqDto request = redisTemplate.opsForSet().pop(QUEUE_KEY);
        if (request == null) {
            throw new RuntimeException("ReservationReqDto 조회 실패");
        }

        request.process(service, requestFutureStore);
    }
}
