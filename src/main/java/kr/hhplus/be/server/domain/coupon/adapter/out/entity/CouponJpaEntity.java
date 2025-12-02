package kr.hhplus.be.server.domain.coupon.adapter.out.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.coupon.core.domain.Coupon;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CouponJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(nullable = false, length = 100)
    private String couponName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private Integer quantity;

    private Integer discountAmount;

    private Integer discountRate;

    // JPA Entity → Domain Model 변환
    public Coupon toDomain() {
        return Coupon.builder()
                .couponId(this.couponId)
                .couponName(this.couponName)
                .createdAt(this.createdAt)
                .expiredAt(this.expiredAt)
                .quantity(this.quantity)
                .discountAmount(this.discountAmount)
                .discountRate(this.discountRate)
                .build();
    }

    // Domain Model → JPA Entity 변환
    public static CouponJpaEntity fromDomain(Coupon coupon) {
        return new CouponJpaEntity(
                coupon.getCouponId(),
                coupon.getCouponName(),
                coupon.getCreatedAt(),
                coupon.getExpiredAt(),
                coupon.getQuantity(),
                coupon.getDiscountAmount(),
                coupon.getDiscountRate()
        );
    }
}
