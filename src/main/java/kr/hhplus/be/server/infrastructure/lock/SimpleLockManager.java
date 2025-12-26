package kr.hhplus.be.server.infrastructure.lock;

import java.util.function.Supplier;

/**
 * Simple Lock 매니저 인터페이스
 */
public interface SimpleLockManager {
    <T> T executeWithSimpleLock(String lockKey, long waitTimeMs, long leaseTimeMs, Supplier<T> supplier);
}
