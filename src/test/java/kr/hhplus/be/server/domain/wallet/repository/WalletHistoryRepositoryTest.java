package kr.hhplus.be.server.domain.wallet.repository;

import kr.hhplus.be.server.common.status.WalletHistoryStatus;
import kr.hhplus.be.server.domain.wallet.model.Wallet;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("WalletHistoryJpaRepository 테스트")
public class WalletHistoryRepositoryTest {

    @Autowired
    private WalletJpaRepository walletRepository;

    @Autowired
    private WalletHistoryJpaRepository walletHistoryRepository;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = Wallet.builder()
                .userId(1L)
                .balance(10000)
                .build();
        walletRepository.save(testWallet);
    }

    @Test
    @DisplayName("WalletHistory 저장 성공")
    void save_Success() {
        // Given
        WalletHistory history = WalletHistory.builder()
                .walletId(testWallet.getWalletId())
                .amount(5000)
                .beforeBalance(10000)
                .afterBalance(15000)
                .status(WalletHistoryStatus.CHARGE)
                .memo("테스트 충전")
                .build();

        // When
        WalletHistory saved = walletHistoryRepository.save(history);

        // Then
        assertThat(saved.getWalletHisId()).isNotNull();
        assertThat(saved.getAmount()).isEqualTo(5000);
        assertThat(saved.getStatus()).isEqualTo(WalletHistoryStatus.CHARGE);
    }

    @Test
    @DisplayName("WalletHistory 전체 조회")
    void findAll() {
        // Given
        WalletHistory history1 = createHistory(testWallet.getWalletId(), WalletHistoryStatus.CHARGE, 5000);
        WalletHistory history2 = createHistory(testWallet.getWalletId(), WalletHistoryStatus.PAYMENT, -3000);
        walletHistoryRepository.save(history1);
        walletHistoryRepository.save(history2);

        // When
        List<WalletHistory> histories = walletHistoryRepository.findAll();

        // Then
        assertThat(histories).hasSize(2);
    }

    @Test
    @DisplayName("WalletHistory ID로 조회")
    void findById() {
        // Given
        WalletHistory history = createHistory(testWallet.getWalletId(), WalletHistoryStatus.CHARGE, 5000);
        WalletHistory saved = walletHistoryRepository.save(history);

        // When
        WalletHistory found = walletHistoryRepository.findById(saved.getWalletHisId()).orElseThrow();

        // Then
        assertThat(found.getWalletHisId()).isEqualTo(saved.getWalletHisId());
        assertThat(found.getAmount()).isEqualTo(5000);
    }

    private WalletHistory createHistory(Long walletId, WalletHistoryStatus status, Integer amount) {
        return WalletHistory.builder()
                .walletId(walletId)
                .amount(amount)
                .beforeBalance(10000)
                .afterBalance(10000 + amount)
                .status(status)
                .memo("테스트 이력")
                .build();
    }
}
