package kr.hhplus.be.server.domain.coupon.core.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Coupon {

    private final Long couponId;
    private final String couponName;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiredAt;
    private final Integer totalQuantity;
    private Integer issuedQuantity;
    private final Long version;
    private final Integer discountAmount;
    private final Integer discountRate;
    private final Boolean isActive;
    private final LocalDateTime startAt;
    private final LocalDateTime updatedAt;

    /*
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
     */

    public Integer calculateDiscount(Integer totalAmount) {
        if (this.discountAmount != null) {
            return this.discountAmount;
        } else if (this.discountRate != null) {
            return totalAmount * this.discountRate / 100;
        }
        return 0;
    }

    /**
     * 쿠폰 발급 가능 여부 확인
     *
     * 검증 순서:
     * 1. 활성화 상태 확인
     * 2. 수량 확인 (핵심)
     * 3. 시작 시간 확인
     * 4. 만료 시간 확인
     *
     * @return true: 발급 가능, false: 발급 불가
     */
    public boolean canIssue() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 비활성화된 쿠폰
        if (!isActive) {
            return false;
        }

        // 2. 수량 초과
        if (issuedQuantity >= totalQuantity) {
            return false;
        }

        // 3. 아직 시작 전
        if (startAt != null && now.isBefore(startAt)) {
            return false;
        }

        // 4. 이미 만료됨
        if (now.isAfter(expiredAt)) {
            return false;
        }

        return true;
    }

    /**
     * 쿠폰 발급 (도메인 로직)
     *
     * 실제 발급 처리:
     * 1. 발급 가능 여부 확인
     * 2. issuedQuantity 증가
     *
     * ⚠️ 주의:
     * - 이 메서드는 메모리에서만 수량을 증가시킴
     * - 실제 DB 저장은 Repository에서 수행
     * - 낙관적 락 충돌은 Repository 계층에서 발생
     *
     * @throws IllegalStateException 발급 불가능한 상태
     */
    public void issue() {
        if (!canIssue()) {
            String reason = getIssueFailureReason();
            throw new IllegalStateException(
                    String.format("쿠폰 발급 불가: %s [name=%s, issued=%d/%d]",
                            reason, couponName, issuedQuantity, totalQuantity)
            );
        }

        this.issuedQuantity++;
    }

    /**
     * 발급 실패 사유 조회
     *
     * @return 발급 불가 사유
     */
    private String getIssueFailureReason() {
        if (!isActive) {
            return "비활성화된 쿠폰";
        }
        if (issuedQuantity >= totalQuantity) {
            return "수량 소진";
        }
        if (isNotStarted()) {
            return "시작 전";
        }
        if (isExpired()) {
            return "만료됨";
        }
        return "알 수 없음";
    }

    // ============================================
    // 비즈니스 로직 - 수량 조회
    // ============================================

    /**
     * 남은 발급 가능 수량
     *
     * @return 남은 수량 (0 이상)
     */
    public int getRemainingQuantity() {
        return Math.max(0, totalQuantity - issuedQuantity);
    }

    /**
     * 발급률 계산 (%)
     *
     * @return 발급률 (0.0 ~ 100.0)
     */
    public double getIssueRate() {
        if (totalQuantity == 0) {
            return 0.0;
        }
        return (issuedQuantity * 100.0) / totalQuantity;
    }

    /**
     * 쿠폰이 매진되었는지 확인
     *
     * @return true: 매진, false: 남음
     */
    public boolean isSoldOut() {
        return issuedQuantity >= totalQuantity;
    }

    // ============================================
    // 비즈니스 로직 - 시간 확인
    // ============================================

    /**
     * 쿠폰이 만료되었는지 확인
     *
     * @return true: 만료됨, false: 유효
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * 쿠폰이 아직 시작되지 않았는지 확인
     *
     * @return true: 아직 시작 안 됨, false: 시작됨 또는 시작 시간 없음
     */
    public boolean isNotStarted() {
        if (startAt == null) {
            return false;  // startAt이 null이면 즉시 사용 가능
        }
        return LocalDateTime.now().isBefore(startAt);
    }

    /**
     * 쿠폰이 현재 유효한지 확인
     * (만료되지 않았고, 시작되었고, 활성화 상태)
     *
     * @return true: 유효, false: 무효
     */
    public boolean isValid() {
        return isActive
                && !isExpired()
                && !isNotStarted();
    }

    // ============================================
    // 비즈니스 로직 - 할인 계산
    // ============================================

    /**
     * 할인 금액 계산
     *
     * 우선순위:
     * 1. discountAmount가 있으면 정액 할인
     * 2. discountRate가 있으면 정률 할인
     * 3. 둘 다 없으면 0원 할인
     *
     * @param originalPrice 원가
     * @return 할인 금액 (음수 불가)
     */
    public int calculateDiscountAmount(int originalPrice) {
        if (originalPrice < 0) {
            throw new IllegalArgumentException("원가는 0 이상이어야 합니다: " + originalPrice);
        }

        // 정액 할인
        if (discountAmount != null && discountAmount > 0) {
            return Math.min(discountAmount, originalPrice);
        }

        // 정률 할인
        if (discountRate != null && discountRate > 0) {
            int discount = originalPrice * discountRate / 100;
            return Math.min(discount, originalPrice);
        }

        return 0;
    }

    /**
     * 할인 적용된 최종 가격 계산
     *
     * @param originalPrice 원가
     * @return 할인 적용 후 가격 (0 이상)
     */
    public int calculateDiscountedPrice(int originalPrice) {
        int discountAmount = calculateDiscountAmount(originalPrice);
        return Math.max(0, originalPrice - discountAmount);
    }

    // ============================================
    // 비즈니스 로직 - 검증
    // ============================================

    /**
     * 쿠폰 도메인 모델 유효성 검증
     *
     * @throws IllegalArgumentException 유효하지 않은 쿠폰
     */
    public void validate() {
        // 이름 검증
        if (couponName == null || couponName.trim().isEmpty()) {
            throw new IllegalArgumentException("쿠폰 이름은 필수입니다");
        }

        // 수량 검증
        if (totalQuantity == null || totalQuantity <= 0) {
            throw new IllegalArgumentException("총 수량은 1 이상이어야 합니다");
        }

        if (issuedQuantity == null || issuedQuantity < 0) {
            throw new IllegalArgumentException("발급 수량은 0 이상이어야 합니다");
        }

        if (issuedQuantity > totalQuantity) {
            throw new IllegalArgumentException(
                    String.format("발급 수량(%d)이 총 수량(%d)을 초과할 수 없습니다",
                            issuedQuantity, totalQuantity)
            );
        }

        // 할인 검증
        if (discountAmount == null && discountRate == null) {
            throw new IllegalArgumentException("할인 금액 또는 할인율 중 하나는 필수입니다");
        }

        if (discountAmount != null && discountAmount < 0) {
            throw new IllegalArgumentException("할인 금액은 0 이상이어야 합니다");
        }

        if (discountRate != null && (discountRate < 0 || discountRate > 100)) {
            throw new IllegalArgumentException("할인율은 0~100 사이여야 합니다");
        }

        // 시간 검증
        if (expiredAt == null) {
            throw new IllegalArgumentException("만료 시간은 필수입니다");
        }

        if (startAt != null && expiredAt.isBefore(startAt)) {
            throw new IllegalArgumentException("만료 시간은 시작 시간 이후여야 합니다");
        }
    }

    // ============================================
    // Object 메서드 오버라이드
    // ============================================

    @Override
    public String toString() {
        return String.format(
                "Coupon[id=%d, name=%s, issued=%d/%d, version=%d, active=%s, expired=%s]",
                couponId,
                couponName,
                issuedQuantity,
                totalQuantity,
                version,
                isActive,
                isExpired() ? "만료" : "유효"
        );
    }
}
