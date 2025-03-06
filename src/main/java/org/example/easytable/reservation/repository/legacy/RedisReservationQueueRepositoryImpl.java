package org.example.easytable.reservation.repository.legacy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
@RequiredArgsConstructor
@Qualifier("redisQueue")
public class RedisReservationQueueRepositoryImpl implements ReservationQueueRepository {
    private static final String WAITING_QUEUE_KEY = "reservation:waiting";
    private static final String PROCESSING_QUEUE_KEY = "reservation:processing";

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Boolean> addToWaitingQueue(String json, double score) {
        return redisTemplate.opsForZSet().add(WAITING_QUEUE_KEY, json, score);
    }

    @Override
    public Flux<String> getWaitingQueueRange(Range<Long> range) {
        return redisTemplate.opsForZSet().range(WAITING_QUEUE_KEY, range);
    }

    @Override
    public Mono<Long> removeFromWaitingQueue(String json) {
        return redisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY, json);
    }

    @Override
    public Mono<Boolean> addToProcessingQueue(String json, double score) {
        return redisTemplate.opsForZSet().add(PROCESSING_QUEUE_KEY, json, score);
    }

    @Override
    public Mono<Long> getProcessingQueueSize() {
        return redisTemplate.opsForZSet().size(PROCESSING_QUEUE_KEY);
    }

    @Override
    public Mono<Long> removeFromProcessingQueue(String json) {
        return redisTemplate.opsForZSet().remove(PROCESSING_QUEUE_KEY, json);
    }
}
