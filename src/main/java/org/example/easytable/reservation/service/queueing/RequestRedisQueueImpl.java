package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.reservation.dto.request.ReservationReqDto;
import org.example.easytable.reservation.service.ReservationService;
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
        /*
        String luaScript = "local current = redis.call('SCARD', KEYS[1]) " +
                "if current < tonumber(ARGV[1]) then " +
                "  redis.call('SADD', KEYS[1], ARGV[2]) " +
                "  return 1 " +
                "else " +
                "  return 0 " +
                "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        // capacity와 request 값을 파라미터로 전달
        Long result = redisTemplate.execute(
                redisScript,
                List.of(QUEUE_KEY),
                String.valueOf(capacity),
                request.toString() // ReservationReqDto가 문자열로 변환 가능한 경우, 혹은 필요한 직렬화 로직 적용
        );

        return result == 1;
         */
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
