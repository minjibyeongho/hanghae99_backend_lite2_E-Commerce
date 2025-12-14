package kr.hhplus.be.server.domain.coupon.core.port.out;

import kr.hhplus.be.server.domain.coupon.core.domain.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponPort {
    Optional<Coupon> findById(Long couponId);
    Optional<Coupon> findByIdWithLock(Long couponId);
    Coupon save(Coupon coupon);
    /**
     * 활성화된 쿠폰 목록 조회
     *
     * @return 활성화된 쿠폰 목록
     */
    List<Coupon> findAllActive();

    /**
     * 쿠폰 삭제 (테스트용)
     *
     * @param couponId 삭제할 쿠폰 ID
     */
    void deleteById(Long couponId);

    /**
     * 모든 쿠폰 삭제 (테스트용)
     */
    void deleteAll();
}
