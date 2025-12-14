package kr.hhplus.be.server.domain.coupon.core.port.out;

import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponPort {
    /**
     * 사용자 쿠폰 ID로 조회
     *
     * @param userCouponId 사용자 쿠폰 ID
     * @return 사용자 쿠폰
     */
    Optional<UserCoupon> findById(Long userCouponId);
    UserCoupon save(UserCoupon userCoupon);

    /**
     * 중복 발급 확인
     * 특정 사용자가 쿠폰을 발급받았나 확인
     * @param userId
     * @param couponId
     * @return
     */
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    /**
     * 사용자의 모든 쿠폰 확인
     * @param userId
     * @return
     */
    List<UserCoupon> findByUserId(Long userId);

    long countByCouponId(Long couponId);

    /**
     * 사용자의 쿠폰 정보 조회
     * @param userId
     * @param couponId
     * @return
     */
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    /**
     * 특정 쿠폰을 발급받은 모든 사용자 조회
     *
     * @param couponId 쿠폰 ID
     * @return 사용자 쿠폰 목록
     */
    List<UserCoupon> findByCouponId(Long couponId);

    /**
     * 사용자 쿠폰 삭제 (테스트용)
     *
     * @param userCouponId 삭제할 사용자 쿠폰 ID
     */
    void deleteById(Long userCouponId);

    /**
     * 모든 사용자 쿠폰 삭제 (테스트용)
     */
    void deleteAll();

}
