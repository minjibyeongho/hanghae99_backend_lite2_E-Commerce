package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.common.TestContainersConfiguration;
import kr.hhplus.be.server.domain.wallet.model.Wallet;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletJpaRepository;
import org.junit.jupiter.api.AfterEach;
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

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
class WalletServiceDistributedLockTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletJpaRepository walletRepository;

    @Autowired
    private WalletHistoryJpaRepository walletHistoryRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // 테스트 지갑 생성 (초기 잔액: 10,000원)
        Wallet wallet = Wallet.builder()
                .userId(1000L)
                .walletName("테스트 지갑")
                .balance(10000)
                .build();

        wallet = walletRepository.save(wallet);
        testUserId = wallet.getUserId();
    }

    @AfterEach
    void tearDown() {
        walletHistoryRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    @DisplayName("Spin Lock - 충전(+) + 결제(-) 동시 실행 (10번 충전 + 10번 결제)")
    void testSpinLock_ChargeAndPayment_Concurrent() throws InterruptedException {
        // Given
        int threadCount = 20;
        int chargeAmount = 1000;  // 충전 금액
        int paymentAmount = 500;  // 결제 금액

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger chargeSuccessCount = new AtomicInteger(0);
        AtomicInteger paymentSuccessCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            executorService.submit(() -> {
                try {
                    if (index % 2 == 0) {
                        // 짝수 스레드: 충전
                        walletService.charge(testUserId, chargeAmount);
                        chargeSuccessCount.incrementAndGet();
                        System.out.println(String.format("[스레드 %d] 충전 성공 (+%d원)", index, chargeAmount));
                    } else {
                        // 홀수 스레드: 결제
                        String idempotencyKey = String.format("payment-test-%d", index);
                        walletService.processPaymentWithIdempotency(testUserId, paymentAmount, idempotencyKey);
                        paymentSuccessCount.incrementAndGet();
                        System.out.println(String.format("[스레드 %d] 결제 성공 (-%d원)", index, paymentAmount));
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println(String.format("[스레드 %d] 실패: %s", index, e.getMessage()));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        Wallet updatedWallet = walletRepository.findByUserId(testUserId).orElseThrow();

        // 예상 최종 잔액 = 초기(10,000) + 충전(10 * 1,000) - 결제(10 * 500)
        int expectedBalance = 10000 + (10 * chargeAmount) - (10 * paymentAmount);

        System.out.println("=".repeat(50));
        System.out.println("📊 테스트 결과");
        System.out.println("=".repeat(50));
        System.out.println(String.format("충전 성공: %d회", chargeSuccessCount.get()));
        System.out.println(String.format("결제 성공: %d회", paymentSuccessCount.get()));
        System.out.println(String.format("실패: %d회", failureCount.get()));
        System.out.println(String.format("최종 잔액: %d원 (예상: %d원)", updatedWallet.getBalance(), expectedBalance));
        System.out.println("=".repeat(50));

        assertThat(chargeSuccessCount.get()).isEqualTo(10);
        assertThat(paymentSuccessCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(0);
        assertThat(updatedWallet.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("Spin Lock - 동일 멱등성 키로 10번 결제 시도 (1번만 차감)")
    void testSpinLock_IdempotencyKey() throws InterruptedException {
        // Given
        int threadCount = 10;
        int paymentAmount = 1000;
        String idempotencyKey = "payment-idempotency-test";

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            executorService.submit(() -> {
                try {
                    WalletHistory history = walletService.processPaymentWithIdempotency(
                            testUserId, paymentAmount, idempotencyKey);

                    successCount.incrementAndGet();
                    System.out.println(String.format(
                            "[스레드 %d] 성공 - walletHisId: %d", index, history.getWalletHisId()));
                } catch (Exception e) {
                    System.out.println(String.format("[스레드 %d] 실패: %s", index, e.getMessage()));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        Wallet updatedWallet = walletRepository.findByUserId(testUserId).orElseThrow();
        long historyCount = walletHistoryRepository.count();

        System.out.println("=".repeat(50));
        System.out.println("📊 멱등성 테스트 결과");
        System.out.println("=".repeat(50));
        System.out.println(String.format("성공 응답: %d회", successCount.get()));
        System.out.println(String.format("최종 잔액: %d원 (예상: 9000원)", updatedWallet.getBalance()));
        System.out.println(String.format("WalletHistory 레코드 수: %d건 (예상: 1건)", historyCount));
        System.out.println("=".repeat(50));

        assertThat(successCount.get()).isEqualTo(10);  // 10개 전부 성공 응답
        assertThat(updatedWallet.getBalance()).isEqualTo(9000);  // 10000 - 1000
        assertThat(historyCount).isEqualTo(1);  // 1건만 저장
    }
}
