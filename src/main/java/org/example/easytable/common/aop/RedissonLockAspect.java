package org.example.easytable.common.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.easytable.common.aop.annotation.RedissonLock;
import org.example.easytable.config.SpelUtil;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
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

    @Value("${spring.data.redis.lock.ttl}")
    private int ttl;

    @Around(value = "@annotation(redissonLock)", argNames = "redissonLock, joinPoint")
    public Object around(RedissonLock redissonLock, ProceedingJoinPoint joinPoint) throws Throwable {
        String evaluatedKey = spelUtil.evaluate(joinPoint, redissonLock.key());
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 트랜잭션 동기화에 등록: 트랜잭션 종료(커밋 또는 롤백) 시점에 락 해제
        if (method.isAnnotationPresent(Transactional.class) &&
                TransactionSynchronizationManager.isActualTransactionActive()
        ) {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(evaluatedKey);
            RLock rLock = redissonLock.readOnly() ? readWriteLock.readLock() : readWriteLock.writeLock();
            // lock 획득 실패 시 내부적으로 Redis pub/sub 기반의 대기
            rLock.lock(ttl, TimeUnit.MILLISECONDS);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // 트랜잭션 종료 후 락 해제
                    if (rLock.isHeldByCurrentThread()) {
                        rLock.unlock();
                    }
                }
            });
        }

        // 비즈니스 로직 수행
        return joinPoint.proceed();
    }
}
