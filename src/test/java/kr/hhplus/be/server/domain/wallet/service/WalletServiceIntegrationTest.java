package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.domain.wallet.model.Wallet;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class WalletServiceIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletJpaRepository walletRepository;

    @Test
    @DisplayName("멱등성 키 테스트 - 동일 키로 10번 요청 시 1번만 처리")
    void test_idempotency_key() throws InterruptedException {
        // Given
        Long userId = 1L;
        Integer amount = 1000;
        String idempotencyKey = "payment_test_001";
        int threadCount = 10;
        int initialBalance = 5000;

        // 지갑 셋팅
        Wallet wallet = Wallet.withId(1L, 1L, "testWallet", 5000);
        walletRepository.save(wallet);

        wallet = walletRepository.findByUserId(userId).orElseThrow();
        System.out.println("초기 잔액: " + wallet.getBalance() + "원");
        assertThat(wallet.getBalance()).isEqualTo(initialBalance);

        // 쓰레드 풀 10개 동작 -> 10번의 동일 요청 실행
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // When: 동일한 멱등성 키로 10번 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    WalletHistory history = walletService.processPaymentWithIdempotency(
                            userId,
                            amount,
                            idempotencyKey
                    );
                    successCount.incrementAndGet();
                    System.out.println("walletHisId: " + history.getWalletHisId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        assertThat(successCount.get()).isEqualTo(threadCount);  // 모두 성공 응답

        // 실제 차감은 1번만 (멱등성 보장)
        assertThat(wallet.getBalance()).isEqualTo(4000);  // 5000 - 1000 = 4000
    }

    @Test
    @DisplayName("조건부 업데이트 테스트 - 잔액 부족 시 실패")
    void test_insufficient_balance() {
        // Given
        Long userId = 1L;
        Integer amount = 10000;  // 잔액(5000원) 초과
        String idempotencyKey = "payment_test_002";

        // When & Then
        assertThatThrownBy(() ->
                walletService.processPaymentWithIdempotency(userId, amount, idempotencyKey)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다");
    }
}
