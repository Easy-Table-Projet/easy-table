package org.example.easytable.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.request.ReservationReqDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

// 사용하려는 구현체에 @Component 활성화시킬 것
// @Component
@RequiredArgsConstructor
public class RequestRedisQueueImpl implements RequestQueue {
    private static final String QUEUE_KEY = "reservation:request:queue";

    private final RedisTemplate<String, ReservationReqDto<?>> redisTemplate;
    private final ReservationService service;

    @Value("${queue-capacity:100}")
    private int capacity;

    @Override
    public boolean enqueue(ReservationReqDto<?> request) {
        double currentTime = System.currentTimeMillis();
        Boolean result = redisTemplate.opsForZSet().add(QUEUE_KEY, request, currentTime);
        return result != null && result;
    }

    @Override
    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        List<ReservationReqDto<?>> requests = redisTemplate.execute(new SessionCallback<List<ReservationReqDto<?>>>() {
            @Override
            public List<ReservationReqDto<?>> execute(RedisOperations operations) throws DataAccessException {
                operations.watch(QUEUE_KEY);

                // 가장 오래된 요청들을 queue size만큼 가져옴
                Set<ReservationReqDto<?>> requestSet = operations.opsForZSet().range(
                        QUEUE_KEY, 0, capacity - 1);

                if (requestSet == null || requestSet.isEmpty()) {
                    operations.unwatch();
                    return Collections.emptyList();
                }

                operations.multi(); // 트랜잭션 시작

                for (ReservationReqDto<?> req : requestSet) {
                    operations.opsForZSet().remove(QUEUE_KEY, req);
                }

                List<Object> execResults = operations.exec();

                if (execResults.isEmpty()) {
                    // 트랜잭션 실패 시 재시도 로직 추가 가능
                    return Collections.emptyList();
                } else {
                    return new ArrayList<>(requestSet);
                }
            }
        });

        for (ReservationReqDto<?> req : requests) {
            req.process(service);
        }
    }
}
