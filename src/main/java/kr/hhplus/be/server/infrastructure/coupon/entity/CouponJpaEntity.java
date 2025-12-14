package kr.hhplus.be.server.infrastructure.coupon.entity;

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

    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer issuedQuantity;

    /**
     * 낙관적 락 버전
     *
     * JPA가 자동으로 관리:
     * - UPDATE 시 version + 1
     * - WHERE 절에 현재 version 포함
     * - version이 다르면 OptimisticLockException
     */
    @Version
    @Column(nullable = false)
    private Long version;

    private Integer discountAmount;

    private Integer discountRate;

    /**
     * 쿠폰 활성화 여부
     * - TRUE: 발급 가능
     * - FALSE: 발급 불가 (관리자가 비활성화)
     */
    @Column(nullable = false)
    private Boolean isActive;

    // ============================================
    // 시간 정보
    // ============================================

    /**
     * 쿠폰 사용 시작 시간
     * - NULL: 즉시 사용 가능
     * - 특정 시간: 해당 시간 이후 사용 가능
     */
    private LocalDateTime startAt;

    /**
     * 마지막 수정 시간 (자동 갱신)
     */
    private LocalDateTime updatedAt;

    /**
     * 쿠폰 발급 가능 여부 확인
     *
     * @return true: 발급 가능, false: 발급 불가
     */
    public boolean canIssue() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 활성화 상태 확인
        if (!isActive) {
            return false;
        }

        // 2. 수량 확인
        if (issuedQuantity >= totalQuantity) {
            return false;
        }

        // 3. 시작 시간 확인 (startAt이 null이면 즉시 사용 가능)
        if (startAt != null && now.isBefore(startAt)) {
            return false;
        }

        // 4. 만료 시간 확인
        if (now.isAfter(expiredAt)) {
            return false;
        }

        return true;
    }

    /**
     * 발급 수량 증가
     *
     * ⚠️ 주의:
     * - 이 메서드 호출 후 save() 하면 낙관적 락 체크 발생
     * - version이 변경되었으면 OptimisticLockException 발생
     *
     * @throws IllegalStateException 발급 불가능한 상태
     */
    public void incrementIssuedQuantity() {
        if (!canIssue()) {
            throw new IllegalStateException(
                    String.format(
                            "쿠폰 발급 불가 [name=%s, issued=%d, total=%d, active=%s, expired=%s]",
                            couponName,
                            issuedQuantity,
                            totalQuantity,
                            isActive,
                            LocalDateTime.now().isAfter(expiredAt) ? "만료됨" : "유효"
                    )
            );
        }

        this.issuedQuantity++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 남은 수량 조회
     *
     * @return 남은 발급 가능 수량
     */
    public int getRemainingQuantity() {
        return Math.max(0, totalQuantity - issuedQuantity);
    }

    /**
     * 쿠폰이 만료되었는지 확인
     *
     * @return true: 만료됨, false: 유효
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * 쿠폰이 아직 시작되지 않았는지 확인
     *
     * @return true: 아직 시작 안 됨, false: 시작됨
     */
    public boolean isNotStarted() {
        return startAt != null && LocalDateTime.now().isBefore(startAt);
    }


    // JPA Entity → Domain Model 변환
    public Coupon toDomain() {
        return Coupon.builder()
//                .couponId(this.couponId)
//                .couponName(this.couponName)
//                .createdAt(this.createdAt)
//                .expiredAt(this.expiredAt)
//                .quantity(this.quantity)
//                .discountAmount(this.discountAmount)
//                .discountRate(this.discountRate)
                .couponId(this.couponId)
                .couponName(this.couponName)
                .totalQuantity(this.totalQuantity)
                .issuedQuantity(this.issuedQuantity)
                .version(this.version)
                .discountAmount(this.discountAmount)
                .discountRate(this.discountRate)
                .isActive(this.isActive)
                .createdAt(this.createdAt)
                .startAt(this.startAt)
                .expiredAt(this.expiredAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    // Domain Model → JPA Entity 변환
    public static CouponJpaEntity fromDomain(Coupon coupon) {
        return new CouponJpaEntity(
                coupon.getCouponId(),
                coupon.getCouponName(),
                coupon.getCreatedAt(),
                coupon.getExpiredAt(),
                coupon.getTotalQuantity(),
                coupon.getIssuedQuantity(),
                coupon.getVersion(),
                coupon.getDiscountAmount(),
                coupon.getDiscountRate(),
                coupon.getIsActive(),
                coupon.getStartAt(),
                coupon.getUpdatedAt()
        );
    }

    // ============================================
    // JPA 생명주기 콜백
    // ============================================

    /**
     * 엔티티 업데이트 전 자동 호출
     * updated_at 자동 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
