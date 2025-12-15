package kr.hhplus.be.server.infrastructure.coupon.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponJpaEntity c WHERE c.couponId = :couponId")
    Optional<CouponJpaEntity> findByIdWithLock(@Param("couponId") Long couponId);

    /**
     * 활성화된 쿠폰 목록 조회
     *
     * @return 활성화된 쿠폰 목록
     */
    @Query("SELECT c FROM CouponJpaEntity c WHERE c.isActive = true")
    List<CouponJpaEntity> findAllActive();
}
