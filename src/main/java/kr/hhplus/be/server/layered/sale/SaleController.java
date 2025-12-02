package kr.hhplus.be.server.layered.sale;

import kr.hhplus.be.server.layered.sale.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    /**
     * 최근 3일 상위 5개 상품 조회 API
     */
    @GetMapping("/top-products")
    public ResponseEntity<List<SaleService.TopProductResponse>> getTopProducts() {
        List<SaleService.TopProductResponse> response = saleService.getTopProductsLast3Days();
        return ResponseEntity.ok(response);
    }
}
