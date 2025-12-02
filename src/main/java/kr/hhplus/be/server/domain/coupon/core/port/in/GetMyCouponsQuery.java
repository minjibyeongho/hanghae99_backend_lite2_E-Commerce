package kr.hhplus.be.server.domain.coupon.core.port.in;

public record GetMyCouponsQuery(Long userId) {
    public GetMyCouponsQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
    }
}
