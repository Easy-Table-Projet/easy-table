package org.example.easytable.reservation.service.legacy;

import lombok.Getter;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.repository.legacy.ReservationQueueRepository;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CreateReservationQueueService {
    private static final int MAX_PROCESSING_QUEUE_LENGTH = 100;

    private final ReservationService reservationService;
    private final ReservationQueueRepository reservationQueue;
    private final SerializerUtil<ReservationCreateReqDto> serializer;

    // 각 예약 요청의 결과를 전달하기 위한 Sinks (예약 ID -> Sinks.One)
    @Getter
    private final ConcurrentHashMap<Long, Sinks.One<ReservationCreateResDto>> resultSinkMap =
            new ConcurrentHashMap<>();

    public CreateReservationQueueService(
            ReservationService reservationService,
            @Qualifier("redisQueue") ReservationQueueRepository reservationQueueRepository,
            SerializerUtil<ReservationCreateReqDto> serializer
    ) {
        this.reservationService = reservationService;
        this.reservationQueue = reservationQueueRepository;
        this.serializer = serializer;
    }

    // 예약 요청을 waiting queue에 저장 (ZSet에 timestamp를 score로 사용)
    public Mono<Boolean> enqueueReservation(ReservationCreateReqDto request) {
        double score = System.currentTimeMillis();
        String json = serializer.serialize(request);
        Sinks.One<ReservationCreateResDto> sink = Sinks.one();
        resultSinkMap.put(request.getRequestId(), sink);
        System.out.println("added Request ID: " + request.getRequestId());
        return reservationQueue.addToWaitingQueue(json, score);
    }

    // 예약 취소: waiting queue에서 해당 예약을 제거
    public Mono<Boolean> cancelReservation(String reservationId) {
        Range<Long> range = Range.of(Bound.inclusive(0L), Bound.unbounded());
        return reservationQueue.getWaitingQueueRange(range)
                .filter(json -> Objects.equals(serializer.getFromJson(json, "id"), reservationId))
                .flatMap(reservationQueue::removeFromWaitingQueue)
                .hasElements();
    }

    // 예약 처리 결과를 기다리는 메서드 (예약 ID에 대응되는 Sinks를 반환)
    public Mono<ReservationCreateResDto> waitForProcessingResult(Long reservationId) {
        Sinks.One<ReservationCreateResDto> sink = resultSinkMap.get(reservationId);
        if (sink != null) {
            return sink.asMono();
        }
        return Mono.empty();
    }

    //@Scheduled(fixedDelay = 1000)
    public void processQueue() {
        Range<Long> waitingRange = Range.of(
                Bound.inclusive(0L), Bound.inclusive((long) MAX_PROCESSING_QUEUE_LENGTH - 1));
        reservationQueue.getWaitingQueueRange(waitingRange)
                .flatMap(this::moveToProcessingQueue)
                .onErrorContinue((throwable, obj) ->
                    System.err.println(
                        "Error processing item in initial phase: " + obj + ", error: " + throwable.getMessage()))
                .concatMap(this::deserializeAndProcess) // 역직렬화 및 처리도 순차적으로 진행
                .subscribe(
                    this::handleResult,
                    error -> System.err.println("Error in processQueue: " + error.getMessage())
                );
    }

    /**
     * waiting queue에서 가져온 JSON 아이템을 processing queue로 옮기는 로직.
     */
    private Mono<String> moveToProcessingQueue(String json) {
        return reservationQueue.removeFromWaitingQueue(json)
            .filter(removed -> removed > 0)
            .flatMap(removed ->
                reservationQueue.getProcessingQueueSize().flatMap(size -> {
                    if (size < MAX_PROCESSING_QUEUE_LENGTH) {
                        double score = System.currentTimeMillis();
                        return reservationQueue.addToProcessingQueue(json, score).thenReturn(json);
                    } else {
                        // processing queue가 최대 크기에 도달한 경우 Mono.empty() 반환
                        return Mono.empty();
                    }
                })
            );
    }

    /**
     * processing queue에 있는 JSON을 역직렬화하고 예약 처리를 진행하는 로직.
     */
    private Mono<ReservationCreateResDto> deserializeAndProcess(String json) {
        return Mono.fromCallable(() -> {
            System.out.println("요청 역직렬화 중");
            return serializer.deserialize(json);
        }).flatMap(request ->
                processReservation(request)
                    .publishOn(Schedulers.boundedElastic())
                    .flatMap(result ->
                        reservationQueue.removeFromProcessingQueue(json).thenReturn(result))
        ).onErrorResume(e -> {
            System.out.println("Deserialization/processing error for json: " + json + ", error: " + e.getMessage());
            return reservationQueue.removeFromProcessingQueue(json).then(Mono.empty());
        });
    }

    /**
     * 처리 결과를 해당 sink에 전달하는 로직.
     */
    private void handleResult(ReservationCreateResDto result) {
        System.out.println("요청 처리 결과 반환 중\nresult: " + result);
        Sinks.One<ReservationCreateResDto> sink = resultSinkMap.remove(result.requestId());
        if (sink == null) {
            throw new IllegalStateException("Sink not found");
        }
        sink.tryEmitValue(result);
    }

    private Mono<ReservationCreateResDto> processReservation(ReservationCreateReqDto request) {
        System.out.println("요청 처리 중");
        return Mono.fromCallable(() -> reservationService.createReservation(request));
    }
}
