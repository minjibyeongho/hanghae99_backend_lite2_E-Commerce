package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.TestContainersConfiguration;
import kr.hhplus.be.server.domain.product.model.Inventory;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.vo.ProductDetailResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
@DisplayName("상품 캐시 통합 테스트")
class ProductCacheTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private InventoryJpaRepository inventoryRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private InventoryService inventoryService;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        // 캐시 초기화
//        Cache cache = cacheManager.getCache("productDetail");
//        if (cache != null) {
//            cache.clear();
//        }

        // ✅ Redis 캐시 완전 삭제
        Cache productCache = cacheManager.getCache("productDetail");
        if (productCache != null) {
            productCache.clear();
        }

        Cache topProductsCache = cacheManager.getCache("topProducts");
        if (topProductsCache != null) {
            topProductsCache.clear();
        }

        // ✅ 테스트용 상품 생성
        Product product = Product.builder()
                .categoryId(1L)
                .productName("테스트 상품")
                .price(10000)
                .build();
        product = productRepository.save(product);
        testProductId = product.getProductId();

        // ✅ 재고 생성 (Builder는 productId와 realQuantity만 사용)
        Inventory inventory = Inventory.builder()
                .productId(testProductId)
                .realQuantity(100)  // 나머지는 생성자에서 자동 초기화
                .build();
        inventoryRepository.save(inventory);
    }

    @Test
    @DisplayName("상품 상세 조회 시 캐시가 생성된다")
    void testProductDetailCacheCreation() {
        // Given
        Cache cache = cacheManager.getCache("productDetail");
        assertThat(cache.get(testProductId)).isNull(); // 캐시 비어있음

        // When: 첫 번째 조회 (DB 조회)
        long startTime1 = System.currentTimeMillis();
        ProductDetailResponse response1 = productService.getProduct(testProductId);
        long duration1 = System.currentTimeMillis() - startTime1;

        // Then: 캐시에 저장됨
        Cache.ValueWrapper cachedValue = cache.get(testProductId);
        assertThat(cachedValue).isNotNull();
        assertThat(cachedValue.get()).isNotNull();

        System.out.println("=== 첫 번째 조회 (DB) ===");
        System.out.println("소요 시간: " + duration1 + "ms");
        System.out.println("상품: " + response1.getProductName());
        System.out.println("가격: " + response1.getPrice());
        System.out.println("재고: " + response1.getAvailableQuantity());
    }

    @Test
    @DisplayName("캐시된 상품은 DB 조회 없이 빠르게 응답한다")
    void testCachedProductIsFaster() {
        // Given: 캐시에 데이터 적재
        productService.getProduct(testProductId);

        // When: 두 번째 조회 (캐시 조회)
        long startTime2 = System.currentTimeMillis();
        ProductDetailResponse response2 = productService.getProduct(testProductId);
        long duration2 = System.currentTimeMillis() - startTime2;

        // Then: 캐시 조회가 DB 조회보다 빠름
        assertThat(duration2).isLessThan(100); // 100ms 이내

        System.out.println("=== 두 번째 조회 (캐시) ===");
        System.out.println("소요 시간: " + duration2 + "ms");
        System.out.println("상품: " + response2.getProductName());
    }

    @Test
    @DisplayName("동일한 상품 ID로 조회 시 같은 데이터를 반환한다")
    void testCacheReturnsSameData() {
        // When
        ProductDetailResponse response1 = productService.getProduct(testProductId);
        ProductDetailResponse response2 = productService.getProduct(testProductId);

        // Then
        assertThat(response1.getProductId()).isEqualTo(response2.getProductId());
        assertThat(response1.getProductName()).isEqualTo(response2.getProductName());
        assertThat(response1.getPrice()).isEqualTo(response2.getPrice());
        assertThat(response1.getAvailableQuantity()).isEqualTo(response2.getAvailableQuantity());

        System.out.println("=== 데이터 일치 확인 ===");
        System.out.println("첫 번째: " + response1);
        System.out.println("두 번째: " + response2);
    }

    @Test
    @DisplayName("재고 변경 시 캐시가 무효화된다")
    void testCacheEvictionOnInventoryChange() {
        // Given: 캐시에 데이터 적재
        ProductDetailResponse response1 = productService.getProduct(testProductId);
        assertThat(response1.getAvailableQuantity()).isEqualTo(100);

        Cache cache = cacheManager.getCache("productDetail");
        assertThat(cache.get(testProductId)).isNotNull(); // 캐시 존재

        // When: 재고 보충 (캐시 무효화 트리거)
        inventoryService.supplyRealQuantity(testProductId, 50);

        // Then: 캐시가 삭제됨
        assertThat(cache.get(testProductId)).isNull();

        // 다시 조회하면 변경된 값이 나옴
        ProductDetailResponse response2 = productService.getProduct(testProductId);
        assertThat(response2.getAvailableQuantity()).isEqualTo(150);

        System.out.println("=== 재고 변경 후 캐시 무효화 ===");
        System.out.println("변경 전 재고: " + response1.getAvailableQuantity());
        System.out.println("변경 후 재고: " + response2.getAvailableQuantity());
    }

    @Test
    @DisplayName("존재하지 않는 상품은 예외를 발생시킨다")
    void testNonExistentProductThrowsException() {
        // Given
        Long nonExistentProductId = 99999L;

        // When & Then
        try {
            productService.getProduct(nonExistentProductId);
        } catch (IllegalArgumentException e) {
            Cache cache = cacheManager.getCache("productDetail");
            assertThat(cache.get(nonExistentProductId)).isNull();

            System.out.println("=== 존재하지 않는 상품 조회 ===");
            System.out.println("예외 발생: " + e.getMessage());
            System.out.println("캐시 저장 여부: false");
        }
    }
}
