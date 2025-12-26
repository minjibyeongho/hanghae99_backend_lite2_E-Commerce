package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.common.TestContainersConfiguration;
import kr.hhplus.be.server.domain.wallet.model.Wallet;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
public class WalletServiceIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletJpaRepository walletRepository;

    @Autowired
    private WalletHistoryJpaRepository walletHistoryRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        walletRepository.deleteAll();
        walletHistoryRepository.deleteAll();
    }

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
//        Wallet wallet = Wallet.withId(1L, 1L, "testWallet", 5000);
//        walletRepository.save(wallet);

        // ✅ 테스트용 지갑 생성 및 저장
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .walletName("testWallet")
                .balance(initialBalance)
                .build();
        walletRepository.save(wallet);

        // 저장 확인
        Wallet savedWallet = walletRepository.findByUserId(userId).orElseThrow();
        System.out.println("초기 잔액: " + savedWallet.getBalance());
        assertThat(savedWallet.getBalance()).isEqualTo(initialBalance);
/*
        wallet = walletRepository.findByUserId(userId).orElseThrow();
        System.out.println("초기 잔액: " + wallet.getBalance() + "원");
        assertThat(wallet.getBalance()).isEqualTo(initialBalance);
*/
        // 쓰레드 풀 10개 동작 -> 10번의 동일 요청 실행
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        // 실패 카운트도 추가
        AtomicInteger failureCount = new AtomicInteger(0);


        // When: 동일한 멱등성 키로 10번 요청
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            executorService.submit(() -> {
                try {
                    WalletHistory history = walletService.processPaymentWithIdempotency(
                            userId,
                            amount,
                            idempotencyKey
                    );
                    successCount.incrementAndGet();
                    System.out.println(String.format(
                            "[스레드 %d] 성공 - walletHisId: %d",
                            threadNum, history.getWalletHisId()));
                    System.out.println("walletHisId: " + history.getWalletHisId());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println(String.format(
                            "[스레드 %d] 실패 - %s: %s",
                            threadNum, e.getClass().getSimpleName(), e.getMessage()));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
/*
        // Then
        assertThat(successCount.get()).isEqualTo(threadCount);  // 모두 성공 응답

        // 실제 차감은 1번만 (멱등성 보장)
        assertThat(wallet.getBalance()).isEqualTo(4000);  // 5000 - 1000 = 4000
*/
        // Then
        System.out.println(String.format(
                "결과: 성공=%d, 실패=%d", successCount.get(), failureCount.get()));
        // ✅ 모든 요청이 성공 응답 (멱등성 키로 인해 동일한 결과 반환)
        assertThat(successCount.get()).isEqualTo(threadCount);

        // ✅ 실제 차감은 1번만 발생 (멱등성 보장)
        Wallet updatedWallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualTo(4000);  // 5000 - 1000 = 4000

        // ✅ WalletHistory는 1건만 생성되어야 함
        long historyCount = walletHistoryRepository.count();
        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    @DisplayName("조건부 업데이트 테스트 - 잔액 부족 시 실패")
    void test_insufficient_balance() {
        // Given
        Long userId = 1L;
        Integer insufficientAmount = 10000;  // 잔액(5000원) 초과
        String idempotencyKey = "payment_test_002";

        // ✅ 테스트용 지갑 생성 (잔액 5000원)
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .walletName("testWallet")
                .balance(5000)
                .build();
        walletRepository.save(wallet);

        // When & Then
        assertThatThrownBy(() ->
                walletService.processPaymentWithIdempotency(userId, insufficientAmount, idempotencyKey)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다");

        // ✅ 잔액이 변경되지 않았는지 확인
        Wallet unchangedWallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(unchangedWallet.getBalance()).isEqualTo(5000);
    }

    @Test
    @DisplayName("서로 다른 멱등성 키는 각각 처리됨")
    void test_different_idempotency_keys() {
        // Given
        Long userId = 1L;
        Integer amount = 1000;

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .walletName("testWallet")
                .balance(10000)
                .build();
        walletRepository.save(wallet);

        // When
        walletService.processPaymentWithIdempotency(userId, amount, "key_001");
        walletService.processPaymentWithIdempotency(userId, amount, "key_002");
        walletService.processPaymentWithIdempotency(userId, amount, "key_003");

        // Then
        Wallet updatedWallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualTo(7000);  // 10000 - 3000

        long historyCount = walletHistoryRepository.count();
        assertThat(historyCount).isEqualTo(3);  // 3건의 이력 생성
    }
}
