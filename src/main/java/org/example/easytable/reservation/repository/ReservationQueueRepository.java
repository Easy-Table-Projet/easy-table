package org.example.easytable.reservation.repository;

import org.springframework.data.domain.Range;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReservationQueueRepository {
    // TODO: 인터페이스로 json 문자열 대신 DTO 객체를 받도록 수정할 것
    Mono<Boolean> addToWaitingQueue(String json, double score);
    Flux<String> getWaitingQueueRange(Range<Long> range);
    Mono<Long> removeFromWaitingQueue(String json);
    Mono<Boolean> addToProcessingQueue(String json, double score);
    Mono<Long> getProcessingQueueSize();
    Mono<Long> removeFromProcessingQueue(String json);
}
