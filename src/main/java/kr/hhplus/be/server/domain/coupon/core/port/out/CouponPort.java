package kr.hhplus.be.server.domain.coupon.core.port.out;

import kr.hhplus.be.server.domain.coupon.core.domain.Coupon;

import java.util.Optional;

public interface CouponPort {
    Optional<Coupon> findById(Long couponId);
    Optional<Coupon> findByIdWithLock(Long couponId);
    Coupon save(Coupon coupon);
}
