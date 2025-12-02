package kr.hhplus.be.server.domain.coupon.core.port.out;

import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponPort {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
    long countByCouponId(Long couponId);
    List<UserCoupon> findByUserId(Long userId);
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
    UserCoupon save(UserCoupon userCoupon);
}
