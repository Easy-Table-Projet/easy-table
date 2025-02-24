package org.example.easytable.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Qualifier("collectionQueue")
public class CollectionReservationQueueRepositoryImpl implements ReservationQueueRepository {
    private final ConcurrentSkipListSet<ReservationCreateReqDto> waitingQueue;
    private final ConcurrentSkipListSet<ReservationCreateReqDto> processingQueue;

    //TODO: 인터페이스 변경에 맞춰 ReservationCreateReqDto 추가 방식도 변경할 것
    @Override
    public Mono<Boolean> addToWaitingQueue(String json, double score) {
        return Mono.just(waitingQueue.add(new ReservationCreateReqDto()));
    }

    @Override
    public Flux<String> getWaitingQueueRange(Range<Long> range) {
        List<ReservationCreateReqDto> sortedList = new ArrayList<>(waitingQueue);
        // lower bound
        int fromIndex = range.getLowerBound().getValue()
                .orElseThrow(() -> new IllegalArgumentException("Range lowerbound not found")).intValue();
        // upper bound: 만약 unbounded라면 전체 크기를 사용, bounded라면 inclusive/exclusive에 따라 조정
        int toIndex = sortedList.size();
        if (range.getUpperBound().isBounded()) {
            Long ub = range.getUpperBound().getValue().orElseThrow(() -> new IllegalArgumentException("Range upperbound not found"));
            toIndex = range.getUpperBound().isInclusive() ? ub.intValue() + 1 : ub.intValue();
        }
        // index 범위 보정
        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.min(sortedList.size(), toIndex);
        List<String> subList = sortedList.subList(fromIndex, toIndex)
                .stream()
                .map(item -> item.json)
                .collect(Collectors.toList());
        return Flux.fromIterable(subList);
    }

    @Override
    public Mono<Long> removeFromWaitingQueue(String json) {
        Optional<ReservationCreateReqDto> target = waitingQueue.stream()
                .filter(item -> item.json.equals(json))
                .findFirst();
        if (target.isPresent()) {
            boolean removed = waitingQueue.remove(target.get());
            return Mono.just(removed ? 1L : 0L);
        }
        return Mono.just(0L);
    }

    @Override
    public Mono<Boolean> addToProcessingQueue(String json, double score) {
        return Mono.just(processingQueue.add(new ReservationCreateReqDto()));
    }

    @Override
    public Mono<Long> getProcessingQueueSize() {
        return Mono.just((long) processingQueue.size());
    }

    @Override
    public Mono<Long> removeFromProcessingQueue(String json) {
        Optional<ReservationCreateReqDto> target = processingQueue.stream()
                .filter(item -> item.json.equals(json))
                .findFirst();
        if (target.isPresent()) {
            boolean removed = processingQueue.remove(target.get());
            return Mono.just(removed ? 1L : 0L);
        }
        return Mono.just(0L);
    }
}
