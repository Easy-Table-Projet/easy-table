package org.example.easytable.reservation.repository;

import org.springframework.data.domain.Range;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReservationQueueRepository {
    Mono<Boolean> addToWaitingQueue(String json, double score);
    Flux<String> getWaitingQueueRange(Range<Long> range);
    Mono<Long> removeFromWaitingQueue(String json);
    Mono<Boolean> addToProcessingQueue(String json, double score);
    Mono<Long> getProcessingQueueSize();
    Mono<Long> removeFromProcessingQueue(String json);
}
