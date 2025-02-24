package org.example.easytable.reservation.service.queueing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CreateReservationQueueService {
    private static final int MAX_PROCESSING_QUEUE_LENGTH = 100;
    private static final String WAITING_QUEUE_KEY = "reservation:waiting";
    private static final String PROCESSING_QUEUE_KEY = "reservation:processing";

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReservationService reservationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 각 예약 요청의 결과를 전달하기 위한 Sinks (예약 ID -> Sinks.One)
    @Getter
    private final ConcurrentHashMap<String, Sinks.One<ReservationCreateResDto>> resultSinkMap =
            new ConcurrentHashMap<>();

    // 예약 요청을 waiting queue에 저장 (ZSet에 timestamp를 score로 사용)
    public Mono<Boolean> enqueueReservation(ReservationCreateReqDto request) {
        double score = System.currentTimeMillis();
        String json = serialize(request);
        Sinks.One<ReservationCreateResDto> sink = Sinks.one();
        resultSinkMap.put(request.getRequestId(), sink);
        System.out.println("added Request ID: " + request.getRequestId());
        return redisTemplate.opsForZSet().add(WAITING_QUEUE_KEY, json, score);
    }

    // 예약 취소: waiting queue에서 해당 예약을 제거
    public Mono<Boolean> cancelReservation(String reservationId) {
        Range<Long> range = Range.of(Bound.inclusive(0L), Bound.unbounded());
        return redisTemplate.opsForZSet().range(WAITING_QUEUE_KEY, range)
                .filter(json -> Objects.equals(getIdFromJson(json), reservationId))
                .flatMap(json -> redisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY, json))
                .hasElements();
    }

    // 예약 처리 결과를 기다리는 메서드 (예약 ID에 대응되는 Sinks를 반환)
    public Mono<ReservationCreateResDto> waitForProcessingResult(String reservationId) {
        Sinks.One<ReservationCreateResDto> sink = resultSinkMap.get(reservationId);
        if (sink != null) {
            return sink.asMono();
        }
        return Mono.empty();
    }

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        Range<Long> range = Range.of(Bound.inclusive(0L), Bound.inclusive(0L));
        redisTemplate.opsForZSet().range(WAITING_QUEUE_KEY, range)
                .flatMap(json ->
                        redisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY, json)
                                .filter(removed -> removed > 0)
                                .flatMap(removed ->
                                        redisTemplate.opsForZSet().size(PROCESSING_QUEUE_KEY)
                                                .flatMap(size -> {
                                                    if (size < MAX_PROCESSING_QUEUE_LENGTH) {
                                                        double score = System.currentTimeMillis();
                                                        return redisTemplate.opsForZSet().add(PROCESSING_QUEUE_KEY, json, score)
                                                                .thenReturn(json);
                                                    } else {
                                                        // PROCESSING_QUEUE가 최대 크기에 도달한 경우 Mono.empty()를 반환하여 추가하지 않음
                                                        return Mono.empty();
                                                    }
                                                })
                                )
                )
                .onErrorContinue((throwable, obj) -> {
                    System.err.println("Error processing item in initial phase: " + obj + ", error: " + throwable.getMessage());
                })
                .flatMap(json ->
                        Mono.fromCallable(() -> {
                                    System.out.println("요청 역직렬화 중");
                                    return deserialize(json);
                                })
                                .flatMap(request ->
                                        processReservation(request)
                                                .publishOn(Schedulers.boundedElastic())
                                                // 처리 완료 후 PROCESSING_QUEUE에서 제거하는 작업을 체인에 포함
                                                .flatMap(result ->
                                                        redisTemplate.opsForZSet().remove(PROCESSING_QUEUE_KEY, json)
                                                                .thenReturn(result)
                                                )
                                )
                                .onErrorResume(e -> {
                                    System.out.println("Deserialization/processing error for json: " + json + ", error: " + e.getMessage());
                                    return redisTemplate.opsForZSet().remove(PROCESSING_QUEUE_KEY, json)
                                            .then(Mono.empty());
                                })
                )
                .subscribe(result -> {
                    System.out.println("요청 처리 결과 반환 중\nresult: " + result);
                    Sinks.One<ReservationCreateResDto> sink = resultSinkMap.remove(result.requestId());
                    if (sink == null) {
                        throw new IllegalStateException("Sink not found");
                    }
                    sink.tryEmitValue(result);
                }, error -> {
                    System.err.println("Error in processQueue: " + error.getMessage());
                });
    }

    private Mono<ReservationCreateResDto> processReservation(ReservationCreateReqDto request) {
        System.out.println("요청 처리 중");
        return Mono.fromCallable(() -> reservationService.createReservation(request));
    }

    public String serialize(ReservationCreateReqDto request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ReservationCreateReqDto deserialize(String json) {
        try {
            ReservationCreateReqDto dto = objectMapper.readValue(json, ReservationCreateReqDto.class);
            System.out.println("deserialized requestId: " + dto.getRequestId());
            return dto;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getIdFromJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.get("id").asText();
        } catch (Exception e) {
            return null;
        }
    }
}
