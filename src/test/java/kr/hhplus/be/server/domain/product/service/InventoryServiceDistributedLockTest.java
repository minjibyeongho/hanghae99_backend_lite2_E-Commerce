package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.TestContainersConfiguration;
import kr.hhplus.be.server.domain.product.model.Inventory;
import kr.hhplus.be.server.domain.product.model.InventoryReservation;
import kr.hhplus.be.server.infrastructure.product.repository.InventoryJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.InventoryReservationJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 재고 서비스 분산락 테스트
 * - Simple Lock (Redisson RLock)
 * - 재고 예약 + 재고 보충 동시 실행
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
public class InventoryServiceDistributedLockTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryJpaRepository inventoryRepository;

    @Autowired
    private InventoryReservationJpaRepository reservationRepository;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        Inventory inventory = Inventory.builder()
                .productId(1000L)
                .realQuantity(100)
                .build();

        inventory = inventoryRepository.save(inventory);
        testProductId = inventory.getProductId();
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        inventoryRepository.deleteAll();
    }

    @Test
    @DisplayName("Simple Lock - 재고 예약(-) + 재고 보충(+) 동시 실행 (10번 예약 + 10번 보충)")
    void testSimpleLock_ReserveAndSupply_Concurrent() throws InterruptedException {
        // Given
        int threadCount = 20;
        int reserveQuantity = 5;
        int supplyQuantity = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger reserveSuccessCount = new AtomicInteger(0);
        AtomicInteger supplySuccessCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            executorService.submit(() -> {
                try {
                    if (index % 2 == 0) {
                        InventoryService.ReserveRequest request =
                                new InventoryService.ReserveRequest(testProductId, reserveQuantity);

                        inventoryService.reserveInventory(1000L, List.of(request));
                        reserveSuccessCount.incrementAndGet();
                        System.out.println(String.format("[스레드 %d] 예약 성공 (-%d개)", index, reserveQuantity));
                    } else {
                        inventoryService.supplyRealQuantity(testProductId, supplyQuantity);
                        supplySuccessCount.incrementAndGet();
                        System.out.println(String.format("[스레드 %d] 보충 성공 (+%d개)", index, supplyQuantity));
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
        Inventory updatedInventory = inventoryRepository.findByProductId(testProductId).orElseThrow();

        int expectedRealQuantity = 100 + (10 * supplyQuantity);
        int expectedReservedQuantity = 10 * reserveQuantity;
        int expectedAvailableQuantity = expectedRealQuantity - expectedReservedQuantity;

        System.out.println("=".repeat(50));
        System.out.println("📊 테스트 결과");
        System.out.println("=".repeat(50));
        System.out.println(String.format("예약 성공: %d회", reserveSuccessCount.get()));
        System.out.println(String.format("보충 성공: %d회", supplySuccessCount.get()));
        System.out.println(String.format("실패: %d회", failureCount.get()));
        System.out.println(String.format("실제 재고: %d개 (예상: %d개)",
                updatedInventory.getRealQuantity(), expectedRealQuantity));
        System.out.println(String.format("예약 재고: %d개 (예상: %d개)",
                updatedInventory.getReservedQuantity(), expectedReservedQuantity));
        System.out.println(String.format("가용 재고: %d개 (예상: %d개)",
                updatedInventory.getAvailableQuantity(), expectedAvailableQuantity));
        System.out.println("=".repeat(50));

        assertThat(reserveSuccessCount.get()).isEqualTo(10);
        assertThat(supplySuccessCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(0);
        assertThat(updatedInventory.getRealQuantity()).isEqualTo(expectedRealQuantity);
        assertThat(updatedInventory.getReservedQuantity()).isEqualTo(expectedReservedQuantity);
        assertThat(updatedInventory.getAvailableQuantity()).isEqualTo(expectedAvailableQuantity);
    }

    @Test
    @DisplayName("Simple Lock - 재고 예약 + 재고 취소 동시 실행")
    void testSimpleLock_ReserveAndCancel_Concurrent() throws InterruptedException {
        // Given
        int threadCount = 10;
        int reserveQuantity = 5;

        ExecutorService setupExecutor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch setupLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            setupExecutor.submit(() -> {
                try {
                    InventoryService.ReserveRequest request =
                            new InventoryService.ReserveRequest(testProductId, reserveQuantity);
                    inventoryService.reserveInventory(1000L, List.of(request));
                } finally {
                    setupLatch.countDown();
                }
            });
        }
        setupLatch.await();
        setupExecutor.shutdown();

        // When
        List<InventoryReservation> reservations = reservationRepository.findAll();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger cancelSuccessCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            executorService.submit(() -> {
                try {
                    inventoryService.cancelReservations(List.of(reservations.get(index)));
                    cancelSuccessCount.incrementAndGet();
                    System.out.println(String.format("[스레드 %d] 취소 성공", index));
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
        Inventory updatedInventory = inventoryRepository.findByProductId(testProductId).orElseThrow();

        System.out.println("=".repeat(50));
        System.out.println("📊 테스트 결과");
        System.out.println("=".repeat(50));
        System.out.println(String.format("취소 성공: %d회", cancelSuccessCount.get()));
        System.out.println(String.format("실제 재고: %d개", updatedInventory.getRealQuantity()));
        System.out.println(String.format("예약 재고: %d개 (예상: 0개)", updatedInventory.getReservedQuantity()));
        System.out.println(String.format("가용 재고: %d개 (예상: 100개)", updatedInventory.getAvailableQuantity()));
        System.out.println("=".repeat(50));

        assertThat(cancelSuccessCount.get()).isEqualTo(10);
        assertThat(updatedInventory.getReservedQuantity()).isEqualTo(0);
        assertThat(updatedInventory.getAvailableQuantity()).isEqualTo(100);
    }
}
