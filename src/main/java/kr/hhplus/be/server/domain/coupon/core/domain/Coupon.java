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
    private final Integer quantity;
    private final Integer discountAmount;
    private final Integer discountRate;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    public Integer calculateDiscount(Integer totalAmount) {
        if (this.discountAmount != null) {
            return this.discountAmount;
        } else if (this.discountRate != null) {
            return totalAmount * this.discountRate / 100;
        }
        return 0;
    }

    public boolean canIssue(long currentIssuedCount) {
        return !isExpired() && currentIssuedCount < quantity;
    }
}
