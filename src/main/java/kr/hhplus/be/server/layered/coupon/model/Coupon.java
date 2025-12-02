package kr.hhplus.be.server.layered.coupon.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(nullable = false)
    private String couponName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private Integer quantity; // 선착순 수량

    private Integer discountAmount;

    private Integer discountRate;

    @Builder
    public Coupon(String couponName, LocalDateTime expiredAt, Integer quantity,
                  Integer discountAmount, Integer discountRate) {
        this.couponName = couponName;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = expiredAt;
        this.quantity = quantity;
        this.discountAmount = discountAmount;
        this.discountRate = discountRate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    // 할인 금액 계산
    public Integer calculateDiscount(Integer totalAmount) {
        if (this.discountAmount != null) {
            return this.discountAmount;
        } else if (this.discountRate != null) {
            return totalAmount * this.discountRate / 100;
        }
        return 0;
    }
}
