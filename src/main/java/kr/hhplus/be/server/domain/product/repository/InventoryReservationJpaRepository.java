package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservation, Long> {
}
