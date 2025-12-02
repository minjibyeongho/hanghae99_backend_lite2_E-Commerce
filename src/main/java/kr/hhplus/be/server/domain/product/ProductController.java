package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    /**
     * 상품 단건 조회 API
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductService.ProductDetailResponse> getProduct(@PathVariable Long productId) {
        ProductService.ProductDetailResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<List<ProductService.ProductDetailResponse>> getAllProducts() {
        List<ProductService.ProductDetailResponse> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }
}
