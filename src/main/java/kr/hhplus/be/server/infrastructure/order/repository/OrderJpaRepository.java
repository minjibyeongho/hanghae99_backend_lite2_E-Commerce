package kr.hhplus.be.server.infrastructure.order.repository;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.infrastructure.order.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);
    List<OrderJpaEntity> findByUserId(Long userId);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.userId = :userId ORDER BY o.orderedAt DESC")
    List<OrderJpaEntity> findByUserIdOrderByOrderedAtDesc(@Param("userId") Long userId);
}
