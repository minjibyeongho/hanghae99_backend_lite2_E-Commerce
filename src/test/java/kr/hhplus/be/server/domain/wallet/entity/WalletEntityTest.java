package kr.hhplus.be.server.domain.wallet.entity;

import kr.hhplus.be.server.domain.wallet.model.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class WalletEntityTest {
    @Test
    @DisplayName("지갑 생성 - 초기 잔액 0")
    void createWallet_WithZeroBalance() {
        // When
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .walletName("테스트 지갑")
                .build();

        // Then
        assertThat(wallet.getBalance()).isEqualTo(0);
        assertThat(wallet.getUserId()).isEqualTo(1L);
        assertThat(wallet.getWalletName()).isEqualTo("테스트 지갑");
    }

    @Test
    @DisplayName("지갑 생성 - 초기 잔액 지정")
    void createWallet_WithInitialBalance() {
        // When
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();

        // Then
        assertThat(wallet.getBalance()).isEqualTo(10000);
    }

    @Test
    @DisplayName("충전 성공")
    void charge_Success() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();

        // When
        Integer newBalance = wallet.addAmount(5000);

        // Then
        assertThat(newBalance).isEqualTo(15000);
        assertThat(wallet.getBalance()).isEqualTo(15000);
    }

    @Test
    @DisplayName("충전 실패 - 음수 금액")
    void charge_Fail_NegativeAmount() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();

        // When & Then
        assertThatThrownBy(() -> wallet.addAmount(-5000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("충전 실패 - null 금액")
    void charge_Fail_NullAmount() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();

        // When & Then
        assertThatThrownBy(() -> wallet.addAmount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("결제 성공")
    void pay_Success() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();

        // When
        Integer newBalance = wallet.substractAmount(3000);

        // Then
        assertThat(newBalance).isEqualTo(7000);
        assertThat(wallet.getBalance()).isEqualTo(7000);
    }

    @Test
    @DisplayName("결제 실패 - 잔액 부족")
    void pay_Fail_InsufficientBalance() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(5000)
                .build();

        // When & Then
        assertThatThrownBy(() -> wallet.substractAmount(10000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다")
                .hasMessageContaining("현재 잔액: 5000원")
                .hasMessageContaining("필요 금액: 10000원");
    }

    @Test
    @DisplayName("결제 실패 - 음수 금액")
    void pay_Fail_NegativeAmount() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();

        // When & Then
        assertThatThrownBy(() -> wallet.substractAmount(-3000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("잔액 충분 여부 확인 - 충분함")
    void hasSufficientBalance_True() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();

        // When & Then
        assertThat(wallet.getBalance() >= 5000).isTrue();
        assertThat(wallet.getBalance() >= 10000).isTrue();
    }

    @Test
    @DisplayName("잔액 충분 여부 확인 - 부족함")
    void hasSufficientBalance_False() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();

        // When & Then
        assertThat(wallet.getBalance() >= 15000).isFalse();
        assertThat(wallet.getBalance() >= 20000).isFalse();
    }

}
