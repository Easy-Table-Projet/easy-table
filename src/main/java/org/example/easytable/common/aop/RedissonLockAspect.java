package org.example.easytable.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.easytable.common.aop.annotation.LockKey;
import org.example.easytable.common.aop.annotation.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(-1) // @Transactional보다 먼저 실행되는 것을 보장
public class RedissonLockAspect {
    private final RedissonClient redissonClient;

    @Value("${spring.data.redis.lock.ttl:2000}")  // 락 유지 시간 (기본값 2초)
    private int ttl;

    @Value("${spring.data.redis.lock.wait:3000}") // 락 대기 시간 (기본값 3초)
    private int waitTime;

    @Around(value = "@annotation(redissonLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        log.debug("🔒 Lock 점유 실행 중...");

        // 1️⃣ `@LockKey`가 붙은 파라미터 값을 가져와 락 키 생성
        String lockKey = generateLockKey(joinPoint, redissonLock);

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 2️⃣ 트랜잭션이 실행 중인지 확인
        if (!method.isAnnotationPresent(Transactional.class) ||
                !TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("🚨 트랜잭션이 적용되고 있지 않습니다.");
        }

        // 3️⃣ 락 획득
        RLock rLock = redissonClient.getLock(lockKey);

        // 4️⃣ 트랜잭션 종료 이후 락 해제 보장
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                    log.debug("🔓 락 해제 완료");
                }
            }
        });

        try {
            // 5️⃣ 락 획득 (대기 시간 & 유지 시간 설정)
            if (rLock.tryLock(waitTime, ttl, TimeUnit.MILLISECONDS)) {
                return joinPoint.proceed(); // 6️⃣ 서비스 로직 실행
            }
            throw new RuntimeException("🚨 락 획득 실패: " + lockKey);
        } catch (InterruptedException e) {
            throw new RuntimeException("🚨 예기치 않은 오류 발생", e);
        }
    }

    /**
     * 🔑 `@LockKey`가 붙은 값을 찾아서 락 키 생성
     */
    private String generateLockKey(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        String prefix = redissonLock.prefix(); // `@RedissonLock(prefix = "...")` 값 가져오기

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof LockKey) {
                    return prefix + args[i]; // 🔑 prefix + `@LockKey`가 붙은 값 사용
                }
            }
        }

        throw new IllegalArgumentException("🚨 `@LockKey`가 붙은 파라미터가 필요합니다.");
    }
}
