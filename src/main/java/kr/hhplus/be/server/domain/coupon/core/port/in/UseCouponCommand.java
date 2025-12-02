package kr.hhplus.be.server.domain.coupon.core.port.in;

public record UseCouponCommand(
        Long userId,
        Long couponId,
        Long orderId
) {
    public UseCouponCommand {
        if (userId == null || couponId == null || orderId == null) {
            throw new IllegalArgumentException("모든 파라미터는 필수입니다.");
        }
    }
}
