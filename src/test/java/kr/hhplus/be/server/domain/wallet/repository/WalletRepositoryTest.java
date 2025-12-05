package kr.hhplus.be.server.domain.wallet.repository;

import kr.hhplus.be.server.domain.wallet.model.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Wallet Repository 테스트")
public class WalletRepositoryTest {
    @Autowired
    private WalletJpaRepository walletRepository;

    @Test
    @DisplayName("userId로 지갑 조회 성공")
    void findByUserId_Success() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .walletName("테스트 지갑")
                .balance(10000)
                .build();

        walletRepository.save(wallet);

        // When
        Optional<Wallet> result = walletRepository.findByUserId(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        assertThat(result.get().getBalance()).isEqualTo(10000);
        assertThat(result.get().getWalletId()).isNotNull();
    }

    @Test
    @DisplayName("userId로 지갑 조회 실패 - 존재하지 않음")
    void findByUserId_NotFound() {
        // When
        Optional<Wallet> result = walletRepository.findByUserId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("지갑 저장 및 ID로 조회")
    void save_And_FindById() {
        // Given
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .walletName("새 지갑")
                .balance(50000)
                .build();

        // When
        Wallet saved = walletRepository.save(wallet);

        // Then
        assertThat(saved.getWalletId()).isNotNull();

        Optional<Wallet> found = walletRepository.findById(saved.getWalletId());
        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualTo(50000);
    }

    @Test
    @DisplayName("여러 사용자의 지갑 조회")
    void findByUserId_MultipleUsers() {
        // Given
        Wallet wallet1 = Wallet.builder().userId(1L).balance(10000).build();
        Wallet wallet2 = Wallet.builder().userId(2L).balance(20000).build();
        walletRepository.save(wallet1);
        walletRepository.save(wallet2);

        // When
        Optional<Wallet> result1 = walletRepository.findByUserId(1L);
        Optional<Wallet> result2 = walletRepository.findByUserId(2L);

        // Then
        assertThat(result1).isPresent();
        assertThat(result1.get().getBalance()).isEqualTo(10000);
        assertThat(result2).isPresent();
        assertThat(result2.get().getBalance()).isEqualTo(20000);
    }
}
