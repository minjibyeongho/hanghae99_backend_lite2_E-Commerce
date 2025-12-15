package kr.hhplus.be.server.domain.coupon.core.facade;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.domain.coupon.core.port.in.IssueCouponCommand;
import kr.hhplus.be.server.domain.coupon.core.usecase.IssueCouponUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueFacade {

    private final IssueCouponUseCase issueCouponUseCase;

    // ============================================
    // 재시도 설정
    // ============================================

    /**
     * 최대 재시도 횟수
     * 동시 요청이 많을수록 높여야 함
     * 예: 1000명 동시 요청 → 50회 재시도
     */
    private static final int MAX_RETRY = 10;

    /**
     * 초기 대기 시간 (밀리초)
     * Exponential Backoff의 기본값
     */
    private static final long BASE_DELAY_MS = 50;

    /**
     * 최대 대기 시간 (밀리초)
     * 너무 오래 기다리지 않도록 제한
     */
    private static final long MAX_DELAY_MS = 1000;

    // ============================================
    // Public API
    // ============================================

    /**
     * 쿠폰 발급 (재시도 포함)
     *
     * @param command 발급 커맨드 (userId, couponId)
     * @return 발급된 사용자 쿠폰
     * @throws IllegalStateException    최대 재시도 초과 또는 비즈니스 예외
     * @throws IllegalArgumentException 잘못된 파라미터
     */
    public UserCoupon issueCouponWithRetry(IssueCouponCommand command) {
        int retryCount = 0;

        while (retryCount < MAX_RETRY) {
            try {
                // ============================================
                // UseCase 실행 (낙관적 락 적용)
                // ============================================
                UserCoupon userCoupon = issueCouponUseCase.execute(command);

                // ============================================
                // 성공 로그 (재시도 있었다면 기록)
                // ============================================
                if (retryCount > 0) {
                    System.out.println(
                            String.format("✅ 쿠폰 발급 성공 (재시도 {}회 후): userId={}, couponId={}",
                                    retryCount, command.userId(), command.couponId())
                    );
                } else {
                    System.out.println(
                            String.format("✅ 쿠폰 발급 성공 (첫 시도): userId={}, couponId={}",
                                    command.userId(), command.couponId())
                    );
                }

                return userCoupon;

            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                // ============================================
                // 낙관적 락 충돌 → 재시도
                // ============================================
                retryCount++;
                System.out.println(
                        String.format("⚠️ 낙관적 락 충돌 (재시도 {}/{}): userId={}, couponId={}, error={}",
                                retryCount, MAX_RETRY, command.userId(), command.couponId(), e.getMessage())
                );

                // 최대 재시도 횟수 초과
                if (retryCount >= MAX_RETRY) {
                    System.out.println(
                            String.format("❌ 최대 재시도 횟수 초과: userId={}, couponId={}, retryCount={}",
                                    command.userId(), command.couponId(), retryCount)
                    );

                    throw new IllegalStateException(
                            String.format(
                                    "쿠폰 발급에 실패했습니다. 잠시 후 다시 시도해주세요. (재시도 %d회)",
                                    retryCount
                            )
                    );
                }

                // ============================================
                // Exponential Backoff: 대기 후 재시도
                // ============================================
                long delay = calculateBackoff(retryCount);

                System.out.println(
                        String.format("⏳ {}ms 대기 후 재시도...", delay)
                );
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.out.println(
                            String.format("❌ 재시도 중단됨: userId={}, couponId={}",
                                    command.userId(), command.couponId())
                    );
                    throw new IllegalStateException("쿠폰 발급이 중단되었습니다", ie);
                }

            } catch (IllegalStateException | IllegalArgumentException e) {
                // ============================================
                // 비즈니스 예외 → 즉시 실패 (재시도 안 함)
                // ============================================
                System.out.println(
                        String.format("❌ 비즈니스 예외 발생 (재시도 {}회 후): userId={}, couponId={}, error={}",
                                retryCount, command.userId(), command.couponId(), e.getMessage())
                );

                // 그대로 던짐 (재시도 안 함)
                throw e;
            }
        }

        // 여기 도달하면 안 됨 (while 문에서 return 또는 throw)
        throw new IllegalStateException("쿠폰 발급 실패 (예상치 못한 경로)");
    }

// ============================================
    // Private 헬퍼 메서드
    // ============================================

    /**
     * Exponential Backoff 대기 시간 계산
     * <p>
     * 공식: delay = BASE_DELAY_MS * (2 ^ retryCount)
     * <p>
     * 예시:
     * - 1회: 50ms
     * - 2회: 100ms
     * - 3회: 200ms
     * - 4회: 400ms
     * - 5회: 800ms
     * - 6회: 1600ms
     * - 7회: 2000ms (MAX_DELAY_MS로 제한)
     *
     * @param retryCount 현재 재시도 횟수
     * @return 대기 시간 (밀리초)
     */
    private long calculateBackoff(int retryCount) {
        // 지수 계산 (오버플로우 방지: 최대 5제곱까지만)
        int exponent = Math.min(retryCount, 5);

        // 2^exponent
        long delay = BASE_DELAY_MS * (1L << exponent);

        // 최대 대기 시간 제한
        return Math.min(delay, MAX_DELAY_MS);
    }

    /**
     * 재시도 통계 조회 (모니터링용, 선택사항)
     */
    public static class RetryStatistics {
        private final int totalAttempts;
        private final int successCount;
        private final int failureCount;
        private final double averageRetries;

        public RetryStatistics(int totalAttempts, int successCount, int failureCount, double averageRetries) {
            this.totalAttempts = totalAttempts;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.averageRetries = averageRetries;
        }

        @Override
        public String toString() {
            return String.format(
                    "RetryStatistics[total=%d, success=%d, failure=%d, avgRetries=%.2f]",
                    totalAttempts, successCount, failureCount, averageRetries
            );
        }
    }
}