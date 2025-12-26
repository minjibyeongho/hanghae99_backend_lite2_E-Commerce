package kr.hhplus.be.server.infrastructure.lock;

import java.util.function.Supplier;

/**
 * Spin Lock 매니저
 * - 락 획득 실패 시 재시도 (busy-waiting)
 * - 짧은 임계 영역에 적합
 */
public interface SpinLockManager {

    /**
     * Spin Lock으로 작업 실행
     *
     * @param lockKey    락 키 (예: "wallet:123:balance")
     * @param waitTimeMs 락 획득 최대 대기 시간 (밀리초)
     * @param leaseTimeMs 락 유지 시간 (밀리초)
     * @param supplier   실행할 작업
     * @return 작업 실행 결과
     * @throws IllegalStateException 락 획득 실패
     */
    <T> T executeWithSpinLock(String lockKey, long waitTimeMs, long leaseTimeMs, Supplier<T> supplier);
}
