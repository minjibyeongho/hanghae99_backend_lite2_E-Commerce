package kr.hhplus.be.server.domain.coupon.adapter.in.web.dto;

public record CouponResponse (
    Long couponId,
    String couponName,
    Integer quantity,
    Integer discountAmount,
    Integer discountRate
){
}
