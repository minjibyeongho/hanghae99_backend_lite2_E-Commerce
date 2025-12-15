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
        Long userId = command.userId();
        Long couponId = command.couponId();

        // 비관적 락에서 낙관적 락으로 변경(쿠폰 조회는 조회가 주로 비관적 락까지는 과함이 있어보임)
        // 1. 중복 발급 확인
        boolean alreadyIssued = userCouponPort.existsByUserIdAndCouponId(userId, couponId);
        if (alreadyIssued) {
            throw new IllegalStateException(
                String.format("이미 발급받은 쿠폰: userId={}, couponId={}", userId, couponId)
            );
        }

        // 2. 낙관적 (일반 조회, version)
        Coupon coupon = couponPort.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));

        // 3. 발급 가능 여부 확인 (내부 필드 사용)
        if (!coupon.canIssue()) {
            throw new IllegalStateException(
                    String.format("쿠폰 발급 불가: couponId={}, issued={}/{}",
                            couponId, coupon.getIssuedQuantity(), coupon.getTotalQuantity())
            );
        }

        // 4. 사용자 쿠폰 생성
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(CouponStatus.PREPARING)
                .issuedAt(LocalDateTime.now())
                .expireAt(coupon.getExpiredAt())
                .createdAt(LocalDateTime.now())
                .build();

        userCoupon = userCouponPort.save(userCoupon);

        // 5. ✅ 쿠폰 발급 수량 증가 (낙관적 락 적용)
        coupon.issue();  // issuedQuantity++

        // ✅ 여기서 version 체크 발생!
        // UPDATE coupon SET issued_quantity = ?, version = version + 1
        // WHERE coupon_id = ? AND version = ?
        couponPort.save(coupon);

        System.out.println(String.format(
                "쿠폰 발급 완료: userId={}, couponId={}, userCouponId={}",
                userId, couponId, userCoupon.getUserCouponId()
        ));

        return userCoupon;
    }
}
