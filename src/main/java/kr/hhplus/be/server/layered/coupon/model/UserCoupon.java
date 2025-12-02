package kr.hhplus.be.server.layered.coupon.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.CouponStatus;
import lombok.AccessLevel;
import lombok.Builder;
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
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userCouponId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long couponId;

    private Long orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UserCoupon(Long userId, Long couponId, CouponStatus status) {
        this.userId = userId;
        this.couponId = couponId;
        this.status = status;
    }

    // 쿠폰 사용
    public void use(Long orderId) {
        if (!(this.status == CouponStatus.PREPARING) ) {
            throw new IllegalStateException("사용 가능한 쿠폰이 아닙니다");
        }
        this.status = CouponStatus.USED;
        this.orderId = orderId;
    }

    // 쿠폰 만료
    public void expire() {
        if (CouponStatus.USED == this.status) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다");
        }
        this.status = CouponStatus.EXPIRED;
    }
}
