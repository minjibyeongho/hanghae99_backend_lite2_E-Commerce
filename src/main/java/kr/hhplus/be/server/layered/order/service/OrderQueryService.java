package kr.hhplus.be.server.layered.order.service;

import kr.hhplus.be.server.layered.order.model.Order;
import kr.hhplus.be.server.layered.order.model.OrderItem;
import kr.hhplus.be.server.layered.order.repository.OrderItemJpaRepository;
import kr.hhplus.be.server.layered.order.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;

    /**
     * 주문 상세 조회 (주문 + 주문 상품)
     */
    public OrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return OrderDetailResponse.of(order, orderItems);
    }

    /**
     * 사용자의 주문 목록 조회 (N+1 방지)
     */
    public List<OrderDetailResponse> getUserOrders(Long userId) {
        // 1. 사용자의 모든 주문 조회
        List<Order> orders = orderRepository.findByUserIdOrderByOrderedAtDesc(userId);

        if (orders.isEmpty()) {
            return List.of();
        }

        // 2. 모든 주문의 ID 추출
        List<Long> orderIds = orders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        // 3. Batch로 모든 주문 상품 조회 (N+1 방지)
        List<OrderItem> allOrderItems = orderItemRepository.findByOrderIdIn(orderIds);

        // 4. orderId로 그룹핑
        Map<Long, List<OrderItem>> orderItemsMap = allOrderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 5. 결과 조합
        return orders.stream()
                .map(order -> OrderDetailResponse.of(
                        order,
                        orderItemsMap.getOrDefault(order.getOrderId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 주문 상품 개수 조회
     */
    public long getOrderItemCount(Long orderId) {
        return orderItemRepository.countByOrderId(orderId);
    }

    /**
     * 주문 상품 총액 계산
     */
    public Integer calculateOrderItemsTotal(Long orderId) {
        return orderItemRepository.sumTotalPriceByOrderId(orderId);
    }

    // Response DTO
    public record OrderDetailResponse(
            Long orderId,
            String orderNumber,
            String orderStatus,
            Integer totalAmount,
            Integer paymentAmount,
            List<OrderItemDto> items
    ) {
        public static OrderDetailResponse of(Order order, List<OrderItem> orderItems) {
            List<OrderItemDto> itemDtos = orderItems.stream()
                    .map(item -> new OrderItemDto(
                            item.getOrderItemId(),
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotalPrice()
                    ))
                    .collect(Collectors.toList());

            return new OrderDetailResponse(
                    order.getOrderId(),
                    order.getOrderNumber(),
                    order.getOrderStatus().name(),
                    order.getTotalAmount(),
                    order.getPaymentAmount(),
                    itemDtos
            );
        }
    }

    public record OrderItemDto(
            Long orderItemId,
            Long productId,
            String productName,
            Integer quantity,
            Integer unitPrice,
            Integer totalPrice
    ) {}

}
