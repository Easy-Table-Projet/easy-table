package org.example.easytable.common.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.easytable.common.aop.annotation.RedissonLock;
import org.example.easytable.config.SpelUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {
    private final RedissonClient redissonClient;
    private final SpelUtil spelUtil;

    @Value("${spring.data.redis.lock.ttl:2000}")
    private int ttl;

    @Value("${spring.data.redis.lock.wait:3000}")
    private int waitTime;

    @Around(value = "@annotation(redissonLock)", argNames = "joinPoint, redissonLock")
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        String evaluatedKey = spelUtil.evaluate(joinPoint, redissonLock.key());
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 트랜잭션이 실행 중일 때만 lock 획득 시도
        if (!method.isAnnotationPresent(Transactional.class) ||
                !TransactionSynchronizationManager.isActualTransactionActive()
        ) { throw new IllegalStateException("트랜잭션이 적용되고 있지 않습니다."); }

        RLock rLock = redissonClient.getLock(evaluatedKey);

        // transaction 종료 이후 lock을 해제하도록 보장
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                // 트랜잭션 종료 후 락 해제
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }
        });

        try {
            // lock 획득 실패 시 waitTime 만큼 내부적으로 Redis pub/sub 기반의 대기
            if (rLock.tryLock(ttl, waitTime, TimeUnit.MILLISECONDS)) {
                // 서비스 로직 수행
                return joinPoint.proceed();
            }
            throw new RuntimeException("lock 획득에 실패함");
        } catch (InterruptedException e) {
            throw new RuntimeException("예기치 않게 예약 처리가 종료됨", e);
        }
    }
}
