package kr.hhplus.be.server.infrastructure.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 기반 Spin Lock 구현
 *
 * 동작 방식:
 * 1. SETNX (SET if Not eXists) 명령으로 락 획득 시도
 * 2. 실패 시 짧은 Sleep 후 재시도 (Spin)
 * 3. 최대 대기 시간 초과 시 예외 발생
 * 4. 락 획득 후 작업 실행 → 락 해제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSpinLockManager implements SpinLockManager {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final long SPIN_SLEEP_MS = 50; // Spin 대기 시간 (50ms)

    @Override
    public <T> T executeWithSpinLock(String lockKey, long waitTimeMs, long leaseTimeMs, Supplier<T> supplier) {
        String lockValue = UUID.randomUUID().toString(); // 락 소유자 식별용
        long startTime = System.currentTimeMillis();

        try {
            // ============================================
            // 1. 락 획득 시도 (Spin Lock)
            // ============================================
            while (System.currentTimeMillis() - startTime < waitTimeMs) {
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, lockValue, leaseTimeMs, TimeUnit.MILLISECONDS);

                if (Boolean.TRUE.equals(acquired)) {
                    log.info("🔒 Spin Lock 획득 성공: lockKey={}, lockValue={}", lockKey, lockValue);

                    try {
                        // ============================================
                        // 2. 작업 실행
                        // ============================================
                        return supplier.get();

                    } finally {
                        // ============================================
                        // 3. 락 해제 (소유자 확인)
                        // ============================================
                        releaseLock(lockKey, lockValue);
                    }
                }

                // ============================================
                // 4. 락 획득 실패 → Spin (짧은 대기 후 재시도)
                // ============================================
                log.debug("⏳ Spin Lock 대기 중: lockKey={}, elapsed={}ms",
                        lockKey, System.currentTimeMillis() - startTime);

                Thread.sleep(SPIN_SLEEP_MS);
            }

            // ============================================
            // 5. 타임아웃 초과
            // ============================================
            log.error("❌ Spin Lock 획득 실패 (타임아웃): lockKey={}, waitTimeMs={}", lockKey, waitTimeMs);
            throw new IllegalStateException(
                    String.format("락 획득에 실패했습니다. 잠시 후 다시 시도해주세요. (lockKey=%s)", lockKey)
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Spin Lock 중단됨: lockKey={}", lockKey, e);
            throw new IllegalStateException("락 획득이 중단되었습니다.", e);
        }
    }

    /**
     * 락 해제 (소유자 확인 후 삭제)
     */
    private void releaseLock(String lockKey, String lockValue) {
        try {
            String currentValue = (String) redisTemplate.opsForValue().get(lockKey);

            // 소유자 확인 (다른 스레드가 획득한 락은 삭제하지 않음)
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
                log.info("🔓 Spin Lock 해제 성공: lockKey={}", lockKey);
            } else {
                log.warn("⚠️ 락 소유자 불일치 (해제 생략): lockKey={}, expected={}, actual={}",
                        lockKey, lockValue, currentValue);
            }
        } catch (Exception e) {
            log.error("❌ Spin Lock 해제 실패: lockKey={}", lockKey, e);
        }
    }
}