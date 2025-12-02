package kr.hhplus.be.server.layered.coupon.service;

import kr.hhplus.be.server.common.status.CouponStatus;
import kr.hhplus.be.server.layered.coupon.model.Coupon;
import kr.hhplus.be.server.layered.coupon.model.UserCoupon;
import kr.hhplus.be.server.layered.coupon.repository.CouponJpaRepository;
import kr.hhplus.be.server.layered.coupon.repository.UserCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponJpaRepository couponRepository;
    private final UserCouponJpaRepository userCouponRepository;

    @Transactional
    public UserCouponResponse issueCoupon(Long userId, Long couponId) {
        // 비관적 락으로 쿠폰 조회
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        // 중복 발급 체크
        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }

        // 만료 체크
        if (coupon.isExpired()) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }

        // 선착순 수량 체크
        long issuedCount = userCouponRepository.countByCouponId(couponId);
        if (issuedCount >= coupon.getQuantity()) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }

        // 발급
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(CouponStatus.PREPARING)
                .build();

        userCouponRepository.save(userCoupon);

        return new UserCouponResponse(
                userCoupon.getUserCouponId(),
                coupon.getCouponName(),
                userCoupon.getStatus().name()
        );
    }

    /**
     * 보유 쿠폰 목록 조회
     */
    public List<UserCouponResponse> getMyCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);

        return userCoupons.stream()
                .map(uc -> {
                    Coupon coupon = couponRepository.findById(uc.getCouponId())
                            .orElse(null);

                    return new UserCouponResponse(
                            uc.getUserCouponId(),
                            coupon != null ? coupon.getCouponName() : "알 수 없음",
                            uc.getStatus().name()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 쿠폰 할인 금액 계산
     */
    public Integer calculateDiscount(Long userId, Long couponId, Integer totalAmount) {
        UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("보유한 쿠폰이 아닙니다"));

        if (userCoupon.getStatus() != CouponStatus.PREPARING) {
            throw new IllegalStateException("사용 가능한 쿠폰이 아닙니다");
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        return coupon.calculateDiscount(totalAmount);
    }

    /**
     * 쿠폰 사용 처리
     */
    @Transactional
    public void useCoupon(Long userId, Long couponId, Long orderId) {
        UserCoupon userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("보유한 쿠폰이 아닙니다"));

        userCoupon.use(orderId);
    }

    // Response DTO
    public record UserCouponResponse(
            Long userCouponId,
            String couponName,
            String status
    ) {}
}
