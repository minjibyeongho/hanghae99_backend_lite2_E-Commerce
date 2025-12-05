package kr.hhplus.be.server.infrastructure.coupon.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.common.status.CouponStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_coupon", columnNames = {"user_id", "coupon_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserCouponJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userCouponId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long couponId;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // JPA Entity → Domain Model 변환
    public UserCoupon toDomain() {
        return UserCoupon.builder()
                .userCouponId(this.userCouponId)
                .userId(this.userId)
                .couponId(this.couponId)
                .orderId(this.orderId)
                .status(this.status)
                .createdAt(this.createdAt)
                .build();
    }

    // Domain Model → JPA Entity 변환
    public static UserCouponJpaEntity fromDomain(UserCoupon userCoupon) {
        return new UserCouponJpaEntity(
                userCoupon.getUserCouponId(),
                userCoupon.getUserId(),
                userCoupon.getCouponId(),
                userCoupon.getOrderId(),
                userCoupon.getStatus(),
                userCoupon.getCreatedAt()
        );
    }
}
