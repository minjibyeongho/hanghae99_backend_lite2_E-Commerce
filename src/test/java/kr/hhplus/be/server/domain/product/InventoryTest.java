package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.model.Inventory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class InventoryTest {
    @Test
    @DisplayName("재고 예약 시 가용 수량이 정확히 계산")
    void testReserve() {
        // Given
        Inventory inventory = Inventory.builder()
                .productId(1L)
                .realQuantity(100)
                .build();

        // When
        inventory.reserve(30);

        // Then
        assertThat(inventory.getRealQuantity()).isEqualTo(100);
        assertThat(inventory.getReservedQuantity()).isEqualTo(30);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(70);  // ✅ 명시적 계산
    }

    @Test
    @DisplayName("예약 확정 시 실재고와 예약이 모두 감소")
    void testConfirmReservation() {
        // Given

        Inventory inventory = Inventory.withAllQuantities(
                        1L, 100, 30, 70
                );

        // When
        inventory.confirmReservation(30);

        // Then
        assertThat(inventory.getRealQuantity()).isEqualTo(70);   // -30
        assertThat(inventory.getReservedQuantity()).isEqualTo(0); // -30
        assertThat(inventory.getAvailableQuantity()).isEqualTo(70); // 유지
    }

    @Test
    @DisplayName("예약 취소 시 가용 수량 복구")
    void testCancelReservation() {
        // Given
        Inventory inventory = Inventory.withAllQuantities(
            1L, 70, 20, 50
        );


        // When
        inventory.cancelReservation(20);

        // Then
        assertThat(inventory.getRealQuantity()).isEqualTo(70);   // 유지
        assertThat(inventory.getReservedQuantity()).isEqualTo(0); // -20
        assertThat(inventory.getAvailableQuantity()).isEqualTo(70); // +20 복구
    }

    @Test
    @DisplayName("가용 수량이 음수가 되면 예외 발생")
    void testNegativeAvailableQuantity() {
        // Given
        Inventory inventory = Inventory.withAllQuantities(
                1L, 10, 0, 10
        );


        // When & Then
        assertThatThrownBy(() -> inventory.reserve(15))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("데이터 불일치 시 검증 예외 발생")
    void testValidationFailure() {
        // Given - 잘못된 상태의 Inventory
        Inventory inventory = Inventory.withAllQuantities(
                1L, 100, 30, 80
        );

        // When & Then
        // reserve() 메서드 실행 시 validateQuantities()에서 예외 발생
        assertThatThrownBy(() -> inventory.reserve(10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("데이터 불일치");
    }
}
