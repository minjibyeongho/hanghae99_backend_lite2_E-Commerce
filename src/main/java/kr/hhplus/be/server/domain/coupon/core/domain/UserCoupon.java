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
    private final CouponStatus status;
    private final LocalDateTime createdAt;

    public boolean canUse() {
        return this.status == CouponStatus.PREPARING;
    }

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
}
