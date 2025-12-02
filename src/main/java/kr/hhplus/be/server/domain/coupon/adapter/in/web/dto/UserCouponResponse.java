package kr.hhplus.be.server.domain.coupon.adapter.in.web.dto;

import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;

public record UserCouponResponse(
        Long userCouponId,
        Long couponId,
        String status
) {
    public static UserCouponResponse from(UserCoupon userCoupon) {
        return new UserCouponResponse(
                userCoupon.getUserCouponId(),
                userCoupon.getCouponId(),
                userCoupon.getStatus().name()
        );
    }
}