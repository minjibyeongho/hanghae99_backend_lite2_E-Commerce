package kr.hhplus.be.server.infrastructure.coupon.persistence;

import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.domain.coupon.core.port.out.UserCouponPort;
import kr.hhplus.be.server.infrastructure.coupon.entity.UserCouponJpaEntity;
import kr.hhplus.be.server.infrastructure.coupon.repository.UserCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserCouponPersistenceAdapter implements UserCouponPort {
    private final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public boolean existsByUserIdAndCouponId(Long userId, Long couponId) {
        return userCouponJpaRepository.existsByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public long countByCouponId(Long couponId) {
        return userCouponJpaRepository.countByCouponId(couponId);
    }

    @Override
    public List<UserCoupon> findByUserId(Long userId) {
        return userCouponJpaRepository.findByUserId(userId).stream()
                .map(UserCouponJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId) {
        return userCouponJpaRepository.findByUserIdAndCouponId(userId, couponId)
                .map(UserCouponJpaEntity::toDomain);
    }

    @Override
    public List<UserCoupon> findByCouponId(Long couponId) {
        return userCouponJpaRepository.findByCouponId(couponId).stream()
                .map(UserCouponJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long userCouponId) {
        userCouponJpaRepository.deleteById(userCouponId);
    }

    @Override
    public void deleteAll() {
        userCouponJpaRepository.deleteAll();
    }

    @Override
    public Optional<UserCoupon> findById(Long userCouponId) {
        return userCouponJpaRepository.findById(userCouponId)
                .map(UserCouponJpaEntity::toDomain);
    }

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        UserCouponJpaEntity entity = UserCouponJpaEntity.fromDomain(userCoupon);
        UserCouponJpaEntity saved = userCouponJpaRepository.save(entity);
        return saved.toDomain();
    }

}
