package kr.hhplus.be.server.layered.product.service;

import kr.hhplus.be.server.common.status.ReservationType;
import kr.hhplus.be.server.layered.product.model.Inventory;
import kr.hhplus.be.server.layered.product.model.InventoryReservation;
import kr.hhplus.be.server.layered.product.repository.InventoryJpaRepository;
import kr.hhplus.be.server.layered.product.repository.InventoryReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    private final InventoryJpaRepository inventoryRepository;
    private final InventoryReservationJpaRepository reservationRepository;

    /**
     * 재고 예약
     */
    @Transactional
    public List<InventoryReservation> reserveInventory(Long userId, List<ReserveRequest> requests) {
        List<InventoryReservation> reservations = new ArrayList<>();

        for (ReserveRequest request : requests) {
            // 비관적 락으로 재고 조회
            Inventory inventory = inventoryRepository.findByProductIdWithLock(request.productId())
                    .orElseThrow(() -> new IllegalArgumentException("재고 정보를 찾을 수 없습니다: 상품 ID " + request.productId()));

            // 재고 부족 체크
            if (inventory.getAvailableQuantity() < request.quantity()) {
                throw new IllegalStateException("재고가 부족합니다: 상품 ID " + request.productId()
                        + " (요청: " + request.quantity() + ", 재고: " + inventory.getAvailableQuantity() + ")");
            }

            // 임시 예약 생성
            InventoryReservation reservation = InventoryReservation.builder()
                    .productId(request.productId())
                    .userId(userId)
                    .quantity(request.quantity())
                    .reservationType(ReservationType.SOFT)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .build();

            reservations.add(reservationRepository.save(reservation));

            // 재고 임시 차감
            inventory.reserveTemporary(request.quantity());
        }

        return reservations;
    }

    /**
     * 예약 확정
     */
    @Transactional
    public void confirmReservations(List<InventoryReservation> reservations, Long orderId) {
        for (InventoryReservation reservation : reservations) {
            Inventory inventory = inventoryRepository.findByProductId(reservation.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("재고 정보를 찾을 수 없습니다"));

            // 예약 확정
            reservation.confirm(orderId);

            // 재고 확정 처리
            inventory.confirmReservation(reservation.getQuantity());
        }
    }

    public record ReserveRequest(Long productId, Integer quantity) {}
}
