package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.domain.wallet.model.Wallet;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService 단위 테스트")
public class WalletServiceTest {
    @Mock
    private WalletJpaRepository walletRepository;

    @Mock
    private WalletHistoryJpaRepository walletHistoryRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    @DisplayName("잔액 충전 성공")
    void charge_Success() {
        // Given
        Long userId = 1L;
        Integer amount = 10000;
        Wallet wallet = Wallet.withId(1L, userId, "테스트 지갑", 5000);

        given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(walletHistoryRepository.save(any(WalletHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // When
        WalletService.WalletChargeResponse response = walletService.charge(userId, amount);

        // Then
        assertThat(response.balance()).isEqualTo(15000);
        assertThat(wallet.getBalance()).isEqualTo(15000);
        verify(walletHistoryRepository).save(any(WalletHistory.class));
    }

    @Test
    @DisplayName("잔액 충전 실패 - 지갑 없음")
    void charge_Fail_WalletNotFound() {
        // Given
        Long userId = 999L;
        given(walletRepository.findByUserId(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> walletService.charge(userId, 10000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지갑을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("잔액 조회 성공")
    void getBalance_Success() {
        // Given
        Long userId = 1L;
        Wallet wallet = Wallet.withId(1L, userId, "테스트 지갑", 20000);

        given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));

        // When
        WalletService.WalletBalanceResponse response = walletService.getBalance(userId);

        // Then
        assertThat(response.walletId()).isEqualTo(1L);
        assertThat(response.balance()).isEqualTo(20000);
    }

    @Test
    @DisplayName("잔액 조회 실패 - 지갑 없음")
    void getBalance_Fail_WalletNotFound() {
        // Given
        Long userId = 999L;
        given(walletRepository.findByUserId(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> walletService.getBalance(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지갑을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("결제 처리 성공")
    void processPayment_Success() {
        // Given
        Long userId = 1L;
        Integer amount = 5000;
        Wallet wallet = Wallet.withId(1L, userId, "테스트 지갑", 10000);

        given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));
        given(walletHistoryRepository.save(any(WalletHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // When
        WalletHistory history = walletService.processPayment(userId, amount);

        // Then
        assertThat(wallet.getBalance()).isEqualTo(5000);
        assertThat(history.getAmount()).isEqualTo(-5000);
        assertThat(history.getBeforeBalance()).isEqualTo(10000);
        assertThat(history.getAfterBalance()).isEqualTo(5000);
        verify(walletHistoryRepository).save(any(WalletHistory.class));
    }

    @Test
    @DisplayName("결제 처리 실패 - 잔액 부족")
    void processPayment_Fail_InsufficientBalance() {
        // Given
        Long userId = 1L;
        Integer amount = 15000;
        Wallet wallet = Wallet.withId(1L, userId, "테스트 지갑", 10000);

        given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));

        // When & Then
        assertThatThrownBy(() -> walletService.processPayment(userId, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다");
    }

    @Test
    @DisplayName("결제 처리 실패 - 지갑 없음")
    void processPayment_Fail_WalletNotFound() {
        // Given
        Long userId = 999L;
        given(walletRepository.findByUserId(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> walletService.processPayment(userId, 5000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지갑을 찾을 수 없습니다.");
    }
}
