package kr.hhplus.be.server.domain.order.repository;

import kr.hhplus.be.server.domain.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.orderedAt DESC")
    List<Order> findByUserIdOrderByOrderedAtDesc(@Param("userId") Long userId);
}
