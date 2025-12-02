package kr.hhplus.be.server.domain.coupon.core.port.in;

public record IssueCouponCommand(
    Long userId,
    Long couponId
) {
    public IssueCouponCommand {
        if (userId == null || couponId == null) {
            throw new IllegalArgumentException("userId와 couponId는 필수입니다.");
        }
    }
}
