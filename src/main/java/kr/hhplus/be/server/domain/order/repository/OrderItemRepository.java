package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.OrderItem;

import java.util.List;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
    List<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
    List<OrderItem> findByUserIdAndProductId(Long userId, Long productId);
    long countByOrderId(Long orderId);
    Integer sumTotalPriceByOrderId(Long orderId);
}
