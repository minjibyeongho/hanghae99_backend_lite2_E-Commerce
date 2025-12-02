package kr.hhplus.be.server.layered.order.repository;

import kr.hhplus.be.server.layered.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
    /**
     * orderId로 주문 상품 목록 조회
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 여러 orderId로 주문 상품 목록 조회 (Batch)
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId IN :orderIds ORDER BY oi.orderId, oi.orderItemId")
    List<OrderItem> findByOrderIdIn(@Param("orderIds") List<Long> orderIds);

    /**
     * orderId와 productId로 조회
     */
    List<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

    /**
     * userId의 특정 상품 주문 이력 조회 (조인)
     */
    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.orderId IN (SELECT o.orderId FROM Order o WHERE o.userId = :userId) " +
            "AND oi.productId = :productId")
    List<OrderItem> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * orderId로 주문 상품 개수 조회
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.orderId = :orderId")
    long countByOrderId(@Param("orderId") Long orderId);

    /**
     * orderId로 주문 상품 총액 계산
     */
    @Query("SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi WHERE oi.orderId = :orderId")
    Integer sumTotalPriceByOrderId(@Param("orderId") Long orderId);
}
