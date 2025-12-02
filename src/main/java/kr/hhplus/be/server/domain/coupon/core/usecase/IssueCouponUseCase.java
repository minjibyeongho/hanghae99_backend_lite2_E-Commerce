package kr.hhplus.be.server.domain.coupon.core.usecase;

import kr.hhplus.be.server.domain.coupon.core.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.domain.coupon.core.port.in.IssueCouponCommand;
import kr.hhplus.be.server.domain.coupon.core.port.out.CouponPort;
import kr.hhplus.be.server.domain.coupon.core.port.out.UserCouponPort;
import kr.hhplus.be.server.common.status.CouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueCouponUseCase {
    private final CouponPort couponPort;
    private final UserCouponPort userCouponPort;

    public UserCoupon execute(IssueCouponCommand command) {
        // 1. 비관적 락으로 쿠폰 조회
        Coupon coupon = couponPort.findByIdWithLock(command.couponId())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        // 2. 중복 발급 체크
        if (userCouponPort.existsByUserIdAndCouponId(command.userId(), command.couponId())) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다");
        }

        // 3. 선착순 수량 체크
        long issuedCount = userCouponPort.countByCouponId(command.couponId());
        if (!coupon.canIssue(issuedCount)) {
            throw new IllegalStateException("쿠폰이 만료되었거나 수량이 소진되었습니다");
        }

        // 4. 발급
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(command.userId())
                .couponId(command.couponId())
                .status(CouponStatus.PREPARING)
                .createdAt(LocalDateTime.now())
                .build();

        return userCouponPort.save(userCoupon);
    }
}
