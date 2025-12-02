package kr.hhplus.be.server.domain.coupon.adapter.out.repository;

import kr.hhplus.be.server.domain.coupon.adapter.out.entity.UserCouponJpaEntity;
import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponJpaEntity, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    long countByCouponId(Long couponId);

    List<UserCouponJpaEntity> findByUserId(Long userId);

    Optional<UserCouponJpaEntity> findByUserIdAndCouponId(Long userId, Long couponId);
}
