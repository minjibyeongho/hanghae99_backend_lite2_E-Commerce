package kr.hhplus.be.server.domain.sale.service;

import kr.hhplus.be.server.common.TestContainersConfiguration;  // ✅ 수정된 import
import kr.hhplus.be.server.common.status.OrderStatus;
import kr.hhplus.be.server.common.status.PaymentMethod;
import kr.hhplus.be.server.common.status.PaymentStatus;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.product.model.Inventory;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.sale.vo.TopProductResponse;
import kr.hhplus.be.server.infrastructure.product.repository.InventoryJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
@DisplayName("인기 상품 캐시 통합 테스트")
class SaleCacheTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private InventoryJpaRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;  // ✅ Clean Architecture용 Repository

    @Autowired
    private OrderItemRepository orderItemRepository;  // ✅ Clean Architecture용 Repository

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // 캐시 초기화
        Cache cache = cacheManager.getCache("topProducts");
        if (cache != null) {
            cache.clear();
        }

        // 테스트 데이터 생성 (최소 5개 상품 필요)
        createTestData();
    }

    @Transactional
    void createTestData() {
        // 5개의 상품 생성
        for (int i = 1; i <= 5; i++) {
            Product product = Product.builder()
                    .categoryId(1L)
                    .productName("테스트 상품 " + i)
                    .price(10000 * i)
                    .build();
            product = productRepository.save(product);

            // 재고 생성
            Inventory inventory = Inventory.builder()
                    .productId(product.getProductId())
                    .realQuantity(1000)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Test
    @DisplayName("인기 상품 조회 시 캐시가 생성된다 (TTL 1시간)")
    void testTopProductsCacheCreation() {
        // Given
        Cache cache = cacheManager.getCache("topProducts");
        assertThat(cache.get("last3days")).isNull(); // 캐시 비어있음

        // When: 첫 번째 조회 (DB 조회)
        long startTime1 = System.currentTimeMillis();
        List<TopProductResponse> response1 = saleService.getTopProductsLast3Days();
        long duration1 = System.currentTimeMillis() - startTime1;

        // Then: 캐시에 저장됨
        Cache.ValueWrapper cachedValue = cache.get("last3days");
        assertThat(cachedValue).isNotNull();

        System.out.println("=== 첫 번째 조회 (DB) ===");
        System.out.println("소요 시간: " + duration1 + "ms");
        System.out.println("조회된 상품 수: " + response1.size());
        response1.forEach(product ->
                System.out.println(String.format("- %s (판매량: %d, 매출: %d원)",
                        product.productName(), product.totalSold(), product.totalRevenue()))
        );
    }

    @Test
    @DisplayName("캐시된 인기 상품은 DB 조회 없이 빠르게 응답한다")
    void testCachedTopProductsIsFaster() {
        // Given: 캐시에 데이터 적재
        saleService.getTopProductsLast3Days();

        // When: 두 번째 조회 (캐시 조회)
        long startTime2 = System.currentTimeMillis();
        List<TopProductResponse> response2 = saleService.getTopProductsLast3Days();
        long duration2 = System.currentTimeMillis() - startTime2;

        // Then: 캐시 조회가 DB 조회보다 빠름
        assertThat(duration2).isLessThan(100); // 100ms 이내

        System.out.println("=== 두 번째 조회 (캐시) ===");
        System.out.println("소요 시간: " + duration2 + "ms");
        System.out.println("캐시에서 조회: " + response2.size() + "개 상품");
    }

    @Test
    @DisplayName("판매 데이터 기록 시 캐시가 무효화된다")
    @Transactional
    void testCacheEvictionOnSaleRecord() {
        // Given: 캐시에 데이터 적재
        saleService.getTopProductsLast3Days();

        Cache cache = cacheManager.getCache("topProducts");
        assertThat(cache.get("last3days")).isNotNull(); // 캐시 존재

        // When: 판매 데이터 기록 (@CacheEvict 동작)
        Product product = productRepository.findAll().get(0); // 첫 번째 상품 사용

        // ✅ Clean Architecture: Domain Model 사용
        Order order = Order.builder()
                .userId(1L)
                .orderNumber("ORD-TEST-001")
                .totalAmount(50000)
                .paymentAmount(50000)
                .paymentMethod(PaymentMethod.WALLET)
                .orderStatus(OrderStatus.PAYMENT_COMPLETED)
                .paymentStatus(PaymentStatus.PAID)
                .orderedAt(Timestamp.valueOf(LocalDateTime.now()))
                .paidAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        order = orderRepository.save(order);  // ✅ OrderRepository가 Mapper를 통해 변환

        OrderItem orderItem = OrderItem.builder()
                .orderId(order.getOrderId())
                .productId(product.getProductId())
                .reservationId(1L)  // ✅ 추가: NOT NULL 제약 조건 충족
                .productName(product.getProductName())
                .quantity(1)
                .unitPrice(50000)
                .totalPrice(50000)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        orderItem = orderItemRepository.save(orderItem);  // ✅ OrderItemRepository 사용

        saleService.recordSales(order, List.of(orderItem));

        // Then: 캐시가 삭제됨
        assertThat(cache.get("last3days")).isNull();

        System.out.println("=== 판매 기록 후 캐시 무효화 ===");
        System.out.println("캐시 상태: 삭제됨");
        System.out.println("다음 조회 시 DB에서 최신 데이터 조회 예정");
    }

    @Test
    @DisplayName("동일한 요청에 대해 같은 인기 상품 목록을 반환한다")
    void testCacheReturnsSameTopProducts() {
        // When
        List<TopProductResponse> response1 = saleService.getTopProductsLast3Days();
        List<TopProductResponse> response2 = saleService.getTopProductsLast3Days();

        // Then
        assertThat(response1.size()).isEqualTo(response2.size());

        for (int i = 0; i < response1.size(); i++) {
            assertThat(response1.get(i).productId()).isEqualTo(response2.get(i).productId());
            assertThat(response1.get(i).totalSold()).isEqualTo(response2.get(i).totalSold());
        }

        System.out.println("=== 데이터 일치 확인 ===");
        System.out.println("첫 번째 조회: " + response1.size() + "개");
        System.out.println("두 번째 조회: " + response2.size() + "개");
    }
}
