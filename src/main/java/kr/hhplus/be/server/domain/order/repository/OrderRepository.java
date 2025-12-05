package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long orderId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdOrderByOrderedAtDesc(Long userId);
}
