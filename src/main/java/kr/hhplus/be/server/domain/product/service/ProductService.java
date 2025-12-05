package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.model.Inventory;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.product.repository.InventoryJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductJpaRepository productRepository;
    private final InventoryJpaRepository inventoryRepository;

    /**
     * 단일 상품 조회 (ID, 이름, 가격, 잔여수량)
     */
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다"));

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("재고 정보를 찾을 수 없습니다"));

        return new ProductDetailResponse(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                inventory.getAvailableQuantity() // 조회 시점 정확한 잔여수량
        );
    }

    /**
     * 전체 상품 목록 조회
     */
    public List<ProductDetailResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(product -> {
                    Inventory inventory = inventoryRepository.findByProductId(product.getProductId())
                            .orElse(null);

                    return new ProductDetailResponse(
                            product.getProductId(),
                            product.getProductName(),
                            product.getPrice(),
                            inventory != null ? inventory.getAvailableQuantity() : 0
                    );
                })
                .collect(Collectors.toList());
    }

    // Response DTO
    public record ProductDetailResponse(
            Long productId,
            String productName,
            Integer price,
            Integer availableQuantity
    ) {}
}
