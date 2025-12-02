package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.domain.order.service.OrderQueryService;
import kr.hhplus.be.server.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;

    /**
     * 주문 및 결제 API
     */
    @PostMapping
    public ResponseEntity<OrderService.OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        OrderService.OrderResponse response = orderService.createOrder(
                request.userId(),
                request.items(),
                request.couponId()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회 API
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderQueryService.OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        OrderQueryService.OrderDetailResponse response = orderQueryService.getOrderDetail(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 주문 목록 조회 API
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderQueryService.OrderDetailResponse>> getUserOrders(@PathVariable Long userId) {
        List<OrderQueryService.OrderDetailResponse> response = orderQueryService.getUserOrders(userId);
        return ResponseEntity.ok(response);
    }

    // Request DTO
    public record CreateOrderRequest(
            Long userId,
            List<OrderService.OrderItemRequest> items,
            Long couponId  // nullable
    ) {}

}
