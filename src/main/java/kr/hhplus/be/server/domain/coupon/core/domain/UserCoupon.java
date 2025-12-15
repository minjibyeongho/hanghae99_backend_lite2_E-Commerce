package kr.hhplus.be.server.domain.coupon.core.domain;

import kr.hhplus.be.server.common.status.CouponStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserCoupon {

    private final Long userCouponId;
    private final Long userId;
    private final Long couponId;

    private final Long orderId;

    private CouponStatus status;

    /**
     * 발급 시간
     */
    private final LocalDateTime issuedAt;

    /**
     * 사용 시간
     */
    private LocalDateTime usedAt;

    /**
     * 만료 시간
     */
    private final LocalDateTime expireAt;

    /**
     * 생성 시간
     */
    private final LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    private LocalDateTime updatedAt;

/*
    public boolean canUse() {
        return this.status == CouponStatus.PREPARING;
    }
*/

    public UserCoupon use(Long orderId) {
        if (!canUse()) {
            throw new IllegalStateException("사용 가능한 쿠폰이 아닙니다");
        }
        return UserCoupon.builder()
                .userCouponId(this.userCouponId)
                .userId(this.userId)
                .couponId(this.couponId)
                .orderId(orderId)
                .status(CouponStatus.USED)
                .createdAt(this.createdAt)
                .build();
    }

    // ============================================
    // 비즈니스 로직
    // ============================================

    /**
     * 쿠폰 사용
     *
     * @throws IllegalStateException 사용 불가능한 상태
     */
    public void use() {
        if (status != CouponStatus.PREPARING) {
            throw new IllegalStateException(
                    String.format("사용 가능한 쿠폰이 아닙니다: status=%s", status)
            );
        }

        if (isExpired()) {
            throw new IllegalStateException("만료된 쿠폰입니다");
        }

        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰 만료 처리
     */
    public void expire() {
        if (status == CouponStatus.USED) {
            throw new IllegalStateException("이미 사용된 쿠폰은 만료 처리할 수 없습니다");
        }

        this.status = CouponStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰이 만료되었는지 확인
     *
     * @return true: 만료됨, false: 유효
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }

    /**
     * 쿠폰 사용 가능 여부
     *
     * @return true: 사용 가능, false: 사용 불가
     */
    public boolean canUse() {
        return status == CouponStatus.PREPARING && !isExpired();
    }

    /**
     * 쿠폰이 사용되었는지 확인
     *
     * @return true: 사용됨, false: 미사용
     */
    public boolean isUsed() {
        return status == CouponStatus.USED;
    }
}
