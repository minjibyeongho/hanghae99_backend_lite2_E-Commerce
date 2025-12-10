package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.status.InventoryReservationStatus;
import kr.hhplus.be.server.common.status.ReservationType;
import kr.hhplus.be.server.domain.product.model.Inventory;
import kr.hhplus.be.server.domain.product.model.InventoryReservation;
import kr.hhplus.be.server.infrastructure.product.repository.InventoryJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.InventoryReservationJpaRepository;
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
            // 비관적 락으로 재고 조회(타 트랜잭션 대기)
            Inventory inventory = inventoryRepository.findByProductIdWithLock(request.productId())
                    .orElseThrow(() -> new IllegalArgumentException("재고 정보를 찾을 수 없습니다: 상품 ID " + request.productId()));

            /*
                JPA Entity로 이관
            // 재고 부족 체크
            if (inventory.getAvailableQuantity() < request.quantity()) {
                throw new IllegalStateException("재고가 부족합니다: 상품 ID " + request.productId()
                        + " (요청: " + request.quantity() + ", 재고: " + inventory.getAvailableQuantity() + ")");
            }
             */

            // 재고 예약(실재고 감소, 예약재고 증가)
            inventory.reserve(request.quantity());

            // 임시 예약 생성
            InventoryReservation reservation = InventoryReservation.builder()
                    .productId(request.productId())
                    .userId(userId)
                    .quantity(request.quantity())
                    .status(InventoryReservationStatus.RESERVED)
                    .reservationType(ReservationType.ORDER)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .build();

            reservations.add(reservationRepository.save(reservation));
        }

        return reservations;
    }

    /**
     * 예약 확정
     */
    @Transactional
    public void confirmReservations(List<InventoryReservation> reservations, Long orderId) {
        for (InventoryReservation reservation : reservations) {
            // 비관적 락으로 조회
            Inventory inventory = inventoryRepository.findByProductId(reservation.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("재고 정보를 찾을 수 없습니다"));

            // 재고 확정 처리
            inventory.confirmReservation(reservation.getQuantity());

            // 예약 확정(실재고 감소, 예약재고 감소)
            reservation.confirm(orderId);
            reservationRepository.save(reservation);
        }
    }

    /**
     *  예약 취소 (비관적 락) - 주문 실패, 결제 실패 시
     */
    @Transactional
    public void cancelReservations(List<InventoryReservation> reservations) {
        for (InventoryReservation reservation : reservations) {

            // 비관적 락으로 조회
            Inventory inventory = inventoryRepository
                    .findByProductIdWithLock(reservation.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다"));

            // 예약 취소 (reserved_quantity 감소, available_quantity 증가)
            inventory.cancelReservation(reservation.getQuantity());

            // 예약 상태 업데이트
            reservation.cancel();
            reservationRepository.save(reservation);
        }
    }

    /**
     * 4. 만료된 예약 정리 (스케줄러에서 호출)
     */
    @Transactional
    public int expireReservations() {
        // RESERVED 상태이면서 만료 시간이 지난 예약 조회
        List<InventoryReservation> expiredReservations =
                reservationRepository.findExpiredReservations(LocalDateTime.now());

        int count = 0;
        for (InventoryReservation reservation : expiredReservations) {
            try {
                // 비관적 락으로 조회
                Inventory inventory = inventoryRepository
                        .findByProductIdWithLock(reservation.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다"));

                // 예약 취소 (재고 복구)
                inventory.cancelReservation(reservation.getQuantity());

                // 예약 만료 처리
                reservation.expire();
                reservationRepository.save(reservation);

                count++;

            } catch (Exception e) {
                throw new IllegalArgumentException(
                    String.format("예약 만료 처리 실패: reservationId={}", reservation.getReservationId())
                );
            }
        }

        return count;
    }

    /**
     *  5. 재고 추가 (입고)
     */
    @Transactional
    public void suppluRealQuantity(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다"));

        inventory.supply(quantity);
    }

    public record ReserveRequest(Long productId, Integer quantity) {}
}
