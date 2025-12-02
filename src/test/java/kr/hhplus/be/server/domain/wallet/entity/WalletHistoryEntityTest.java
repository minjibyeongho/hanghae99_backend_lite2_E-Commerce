package kr.hhplus.be.server.domain.wallet.entity;

import kr.hhplus.be.server.common.status.WalletHistoryStatus;
import kr.hhplus.be.server.domain.wallet.model.Wallet;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WalletHistory Entity 테스트")
public class WalletHistoryEntityTest {

    @Test
    @DisplayName("충전 이력 생성")
    void createChargeHistory() {
        // Given
        Wallet wallet = Wallet.withId(1L, 1L, "테스트 지갑", 15000);
        Integer amount = 5000;
        Integer beforeBalance = 10000;

        // When
        WalletHistory history = WalletHistory.createChargeHistory(wallet, amount, beforeBalance);

        // Then
        assertThat(history.getWalletId()).isEqualTo(1L);
        assertThat(history.getAmount()).isEqualTo(5000);
        assertThat(history.getBeforeBalance()).isEqualTo(10000);
        assertThat(history.getAfterBalance()).isEqualTo(15000);
        assertThat(history.getStatus()).isEqualTo(WalletHistoryStatus.CHARGE);
        assertThat(history.getMemo()).isEqualTo("잔액 충전");
    }

    @Test
    @DisplayName("결제 이력 생성 - 금액은 음수로 저장")
    void createPaymentHistory() {
        // Given
        Wallet wallet = Wallet.withId(1L, 1L, "테스트 지갑", 7000);
        Integer amount = 3000;
        Integer beforeBalance = 10000;

        // When
        WalletHistory history = WalletHistory.createPaymentHistory(wallet, amount, beforeBalance);

        // Then
        assertThat(history.getAmount()).isEqualTo(-3000);  // 음수
        assertThat(history.getBeforeBalance()).isEqualTo(10000);
        assertThat(history.getAfterBalance()).isEqualTo(7000);
        assertThat(history.getStatus()).isEqualTo(WalletHistoryStatus.PAYMENT);
        assertThat(history.getMemo()).isEqualTo("결제 차감");
    }

    @Test
    @DisplayName("잔액 변동 확인 - 증가")
    void getBalanceChange_Increased() {
        // Given
        Wallet wallet = Wallet.withId(1L, 1L, "테스트 지갑", 15000);
        WalletHistory history = WalletHistory.createChargeHistory(wallet, 5000, 10000);

        // When & Then
        assertThat(history.getBalanceChange()).isEqualTo(5000);
    }

    @Test
    @DisplayName("잔액 변동 확인 - 감소")
    void getBalanceChange_Decreased() {
        // Given
        Wallet wallet = Wallet.withId(1L, 1L, "테스트 지갑", 7000);
        WalletHistory history = WalletHistory.createPaymentHistory(wallet, 3000, 10000);

        // When & Then
        assertThat(history.getBalanceChange()).isEqualTo(-3000);
    }

}
