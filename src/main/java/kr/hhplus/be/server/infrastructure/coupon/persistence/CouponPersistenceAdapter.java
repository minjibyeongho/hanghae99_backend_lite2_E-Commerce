package kr.hhplus.be.server.infrastructure.coupon.persistence;

import kr.hhplus.be.server.domain.coupon.core.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.core.port.out.CouponPort;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponJpaEntity;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponPersistenceAdapter implements CouponPort {
    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Optional<Coupon> findById(Long couponId) {
        return couponJpaRepository.findById(couponId)
                .map(CouponJpaEntity::toDomain);
    }

    @Override
    public Optional<Coupon> findByIdWithLock(Long couponId) {
        return couponJpaRepository.findByIdWithLock(couponId)
                .map(CouponJpaEntity::toDomain);
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponJpaEntity entity = CouponJpaEntity.fromDomain(coupon);
        CouponJpaEntity saved = couponJpaRepository.save(entity);
        return saved.toDomain();
    }
}
