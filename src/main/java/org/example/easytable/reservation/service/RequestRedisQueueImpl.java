package org.example.easytable.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.reservation.dto.request.ReservationReqDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


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
        double currentTime = System.currentTimeMillis();
        Boolean result = redisTemplate.opsForZSet().add(QUEUE_KEY, request, currentTime);
        log.debug("queued Request: {}", request);
        return result != null && result;
    }

    @Override
    // @Scheduled(fixedDelay = 10000)
    public synchronized void processQueue() {
        ReservationReqDto request = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public ReservationReqDto execute(RedisOperations operations) throws DataAccessException {
                operations.watch(QUEUE_KEY);

                // 가장 오래된 하나의 요청을 가져옴
                Set<ReservationReqDto> requestSet = operations.opsForZSet().range(QUEUE_KEY, 0, 0);

                if (requestSet == null || requestSet.isEmpty()) {
                    operations.unwatch();
                    throw new RuntimeException("ReservationReqDto 조회 실패");
                }

                ReservationReqDto req = requestSet.iterator().next();

                operations.multi(); // 트랜잭션 시작

                operations.opsForZSet().remove(QUEUE_KEY, req);

                List<Object> execResults = operations.exec();

                if (execResults.isEmpty()) {
                    System.out.println("Error in execution");
                    return null;
                }

                return req;
            }
        });

        request.process(service, requestFutureStore);
    }
}
