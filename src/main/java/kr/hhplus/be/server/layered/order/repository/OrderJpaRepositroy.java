package kr.hhplus.be.server.layered.order.repository;

import kr.hhplus.be.server.layered.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderJpaRepositroy extends JpaRepository<Order, Long> {
}
