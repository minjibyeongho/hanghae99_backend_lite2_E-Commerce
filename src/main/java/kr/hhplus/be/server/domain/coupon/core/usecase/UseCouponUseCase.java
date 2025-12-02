package kr.hhplus.be.server.domain.coupon.core.usecase;

import kr.hhplus.be.server.domain.coupon.core.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.domain.coupon.core.port.in.UseCouponCommand;
import kr.hhplus.be.server.domain.coupon.core.port.out.CouponPort;
import kr.hhplus.be.server.domain.coupon.core.port.out.UserCouponPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UseCouponUseCase {
    private final UserCouponPort userCouponPort;
    private final CouponPort couponPort;

    public Integer calculateDiscount(Long userId, Long couponId, Integer totalAmount) {
        UserCoupon userCoupon = userCouponPort.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("보유한 쿠폰이 아닙니다"));

        if (!userCoupon.canUse()) {
            throw new IllegalStateException("사용 가능한 쿠폰이 아닙니다");
        }

        Coupon coupon = couponPort.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        return coupon.calculateDiscount(totalAmount);
    }

    public UserCoupon execute(UseCouponCommand command) {
        UserCoupon userCoupon = userCouponPort.findByUserIdAndCouponId(
                command.userId(),
                command.couponId()
        ).orElseThrow(() -> new IllegalArgumentException("보유한 쿠폰이 아닙니다"));

        UserCoupon usedCoupon = userCoupon.use(command.orderId());
        return userCouponPort.save(usedCoupon);
    }
}
