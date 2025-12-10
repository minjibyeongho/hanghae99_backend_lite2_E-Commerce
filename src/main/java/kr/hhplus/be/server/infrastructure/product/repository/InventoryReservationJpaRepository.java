package kr.hhplus.be.server.infrastructure.product.repository;

import kr.hhplus.be.server.common.status.InventoryReservationStatus;
import kr.hhplus.be.server.domain.product.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryReservationJpaRepository extends JpaRepository<InventoryReservation, Long> {

    // 사용자별 예약 조회
    List<InventoryReservation> findByUserId(Long userId);

    // 주문별 예약 조회
    List<InventoryReservation> findByOrderId(Long orderId);

    // 상품별 예약 조회
    List<InventoryReservation> findByProductId(Long productId);

    // ✅ 만료된 예약 조회 (스케줄러용)
    @Query("SELECT ir FROM InventoryReservation ir " +
            "WHERE ir.status = 'RESERVED' " +
            "AND ir.expiresAt < :now")
    List<InventoryReservation> findExpiredReservations(@Param("now") LocalDateTime now);

    // 특정 상태의 예약 조회
    List<InventoryReservation> findByStatus(InventoryReservationStatus status);
}
