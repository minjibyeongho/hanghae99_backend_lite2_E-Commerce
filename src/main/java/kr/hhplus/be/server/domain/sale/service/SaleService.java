package kr.hhplus.be.server.domain.sale.service;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.domain.product.repository.ProductJpaRepository;
import kr.hhplus.be.server.domain.sale.model.Sale;
import kr.hhplus.be.server.domain.sale.repository.SaleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleService {
    private final SaleJpaRepository saleRepository;
    private final ProductJpaRepository productRepository;

    /**
     * 주문 완료 시 판매 기록 생성
     */
    @Transactional
    public void recordSales(Order order, List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElse(null);

            Sale sale = Sale.builder()
                    .productId(item.getProductId())
                    .orderId(order.getOrderId())
                    .productName(item.getProductName())
                    .soldQuantity(item.getQuantity())
                    .soldTotalAmount(item.getTotalPrice())
                    .soldAt(order.getPaidAt() != null ? order.getPaidAt() : Timestamp.valueOf(LocalDateTime.now()))
                    .categoryId(product != null ? product.getCategoryId() : null)
                    .categoryName(null)
                    .build();

            saleRepository.save(sale);
        }
    }

    /**
     * 최근 3일간 상위 5개 상품 조회
     */
    public List<TopProductResponse> getTopProductsLast3Days() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(3);
        Pageable topFive = PageRequest.of(0, 5);

        List<SaleJpaRepository.TopProductProjection> results =
                saleRepository.findTopProductsSince(startDate, topFive);

        return results.stream()
                .map(proj -> new TopProductResponse(
                        proj.getProductId(),
                        proj.getProductName(),
                        proj.getTotalSold(),
                        proj.getTotalRevenue()
                ))
                .collect(Collectors.toList());
    }

    // Response DTO
    public record TopProductResponse(
            Long productId,
            String productName,
            Long totalSold,
            Long totalRevenue
    ) {}

}
