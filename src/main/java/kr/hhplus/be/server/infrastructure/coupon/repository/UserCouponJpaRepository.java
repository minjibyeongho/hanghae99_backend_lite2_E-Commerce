package kr.hhplus.be.server.infrastructure.coupon.repository;

import kr.hhplus.be.server.infrastructure.coupon.entity.UserCouponJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponJpaEntity, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    long countByCouponId(Long couponId);

    List<UserCouponJpaEntity> findByUserId(Long userId);

    Optional<UserCouponJpaEntity> findByUserIdAndCouponId(Long userId, Long couponId);

    Collection<UserCouponJpaEntity> findByCouponId(Long couponId);
}
