package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.core.domain.Coupon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

public class CouponTest {

    @Test
    @DisplayName("발급 가능한 쿠폰")
    void canIssue_validCoupon_true() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponName("테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(50)
                .version(0L)
                .discountAmount(1000)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .startAt(LocalDateTime.now().minusHours(1))
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        // When & Then
        assertThat(coupon.canIssue()).isTrue();
    }

    @Test
    @DisplayName("수량 소진된 쿠폰")
    void canIssue_soldOut_false() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponName("품절 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)  // 이미 다 발급됨
                .version(0L)
                .discountAmount(1000)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        // When & Then
        assertThat(coupon.canIssue()).isFalse();
        assertThat(coupon.isSoldOut()).isTrue();
    }

    @Test
    @DisplayName("쿠폰 발급 시 수량 증가")
    void issue_increaseQuantity() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponName("테스트 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(50)
                .version(0L)
                .discountAmount(1000)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        // When
        coupon.issue();

        // Then
        assertThat(coupon.getIssuedQuantity()).isEqualTo(51);
    }

    @Test
    @DisplayName("발급 불가능한 쿠폰에 발급 시도 → 예외")
    void issue_invalidCoupon_exception() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponName("품절 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(100)
                .version(0L)
                .discountAmount(1000)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        // When & Then
        assertThatThrownBy(() -> coupon.issue())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("쿠폰 발급 불가");
    }

    @Test
    @DisplayName("할인 금액 계산 - 정액 할인")
    void calculateDiscountAmount_fixedAmount() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponName("1000원 할인")
                .totalQuantity(100)
                .issuedQuantity(0)
                .version(0L)
                .discountAmount(1000)  // 정액
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        // When & Then
        assertThat(coupon.calculateDiscountAmount(5000)).isEqualTo(1000);
        assertThat(coupon.calculateDiscountAmount(500)).isEqualTo(500);  // 원가보다 클 수 없음
    }

    @Test
    @DisplayName("할인 금액 계산 - 정률 할인")
    void calculateDiscountAmount_percentage() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponName("10% 할인")
                .totalQuantity(100)
                .issuedQuantity(0)
                .version(0L)
                .discountRate(10)  // 10%
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        // When & Then
        assertThat(coupon.calculateDiscountAmount(10000)).isEqualTo(1000);
        assertThat(coupon.calculateDiscountAmount(5000)).isEqualTo(500);
    }

}
