package kr.hhplus.be.server.infrastructure.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 기반 Simple Lock 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSimpleLockManager implements SimpleLockManager {

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithSimpleLock(String lockKey, long waitTimeMs, long leaseTimeMs, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(waitTimeMs, leaseTimeMs, TimeUnit.MILLISECONDS);

            if (!acquired) {
                log.error("❌ Simple Lock 획득 실패 (타임아웃): lockKey={}, waitTimeMs={}", lockKey, waitTimeMs);
                throw new IllegalStateException(
                        String.format("락 획득에 실패했습니다. 잠시 후 다시 시도해주세요. (lockKey=%s)", lockKey)
                );
            }

            log.info("🔒 Simple Lock 획득 성공: lockKey={}, threadId={}",
                    lockKey, Thread.currentThread().getId());

            try {
                return supplier.get();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("🔓 Simple Lock 해제 성공: lockKey={}", lockKey);
                } else {
                    log.warn("⚠️ 락 소유자가 아님 (해제 생략): lockKey={}", lockKey);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Simple Lock 중단됨: lockKey={}", lockKey, e);
            throw new IllegalStateException("락 획득이 중단되었습니다.", e);
        }
    }
}
