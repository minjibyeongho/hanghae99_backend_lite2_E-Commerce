package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.status.InventoryReservationStatus;
import kr.hhplus.be.server.common.status.ReservationType;
import kr.hhplus.be.server.domain.product.model.Inventory;
import kr.hhplus.be.server.domain.product.model.InventoryReservation;
import kr.hhplus.be.server.infrastructure.lock.SimpleLockManager;
import kr.hhplus.be.server.infrastructure.product.repository.InventoryJpaRepository;
import kr.hhplus.be.server.infrastructure.product.repository.InventoryReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryJpaRepository inventoryRepository;
    private final InventoryReservationJpaRepository reservationRepository;
    private final SimpleLockManager simpleLockManager;
    private final ObjectProvider<InventoryService> selfProvider;  // ✅ ObjectProvider 사용  // ✅ 자기 자신 주입 (프록시)

    private static final long LOCK_WAIT_TIME_MS = 5000;
    private static final long LOCK_LEASE_TIME_MS = 5000;

    /**
     * 재고 예약-1, 비관적 락 적용
     */
/*
    @Transactional
    public List<InventoryReservation> reserveInventory(Long userId, List<ReserveRequest> requests) {
        List<InventoryReservation> reservations = new ArrayList<>();

        for (ReserveRequest request : requests) {
            // 비관적 락으로 재고 조회(타 트랜잭션 대기)
            Inventory inventory = inventoryRepository.findByProductIdWithLock(request.productId())
                    .orElseThrow(() -> new IllegalArgumentException("재고 정보를 찾을 수 없습니다: 상품 ID " + request.productId()));

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
*/

    /**
     * 재고 예약 - 심플락 적용(분산락 단원)
     */
    @Transactional(readOnly = true)
    public List<InventoryReservation> reserveInventory(Long userId, List<ReserveRequest> requests) {
        List<InventoryReservation> reservations = new ArrayList<>();

        for (ReserveRequest request : requests) {
            String lockKey = String.format("inventory:%d:modify", request.productId());

            // ✅ Simple Lock 적용
            InventoryReservation reservation = simpleLockManager.executeWithSimpleLock(
                    lockKey,
                    LOCK_WAIT_TIME_MS,
                    LOCK_LEASE_TIME_MS,
                    () -> selfProvider.getObject().reserveInventoryInternal(userId, request)
            );

            reservations.add(reservation);
        }

        return reservations;
    }

    // ✅ 내부 로직 분리
    @Transactional(propagation = Propagation.REQUIRES_NEW)  // ✅ 새로운 트랜잭션
    public InventoryReservation reserveInventoryInternal(Long userId, ReserveRequest request) {

        // 재고 조회
        Inventory inventory = inventoryRepository.findByProductId(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("재고 정보를 찾을 수 없습니다: 상품 ID " + request.productId()));

        // 2. 재고 검증
        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new IllegalStateException(String.format(
                    "재고가 부족합니다. 상품 ID: %d, 요청 수량: %d, 가용 재고: %d",
                    request.productId(), request.quantity(), inventory.getAvailableQuantity()
            ));
        }

        // 재고 예약(실재고 감소, 예약재고 증가)
        inventory.reserve(request.quantity());
        inventoryRepository.save(inventory);

        // 임시 예약 생성
        InventoryReservation reservation = InventoryReservation.builder()
                .productId(request.productId())
                .userId(userId)
                .quantity(request.quantity())
                .status(InventoryReservationStatus.RESERVED)
                .reservationType(ReservationType.ORDER)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        reservationRepository.save(reservation);
        return reservation;
    }

    /**
     * 예약 확정 - 심플락 적용
     */
    @Transactional(readOnly = true)
    public void confirmReservations(List<InventoryReservation> reservations, Long orderId) {
        for (InventoryReservation reservation : reservations) {
            String lockKey = String.format("inventory:%d:modify", reservation.getProductId());

            simpleLockManager.executeWithSimpleLock(
                    lockKey,
                    LOCK_WAIT_TIME_MS,
                    LOCK_LEASE_TIME_MS,
                    () -> {
                        selfProvider.getObject().confirmReservationInternal(reservation, orderId);
                        return null;
                    }
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmReservationInternal(InventoryReservation reservation, Long orderId) {
        Inventory inventory = inventoryRepository.findByProductId(reservation.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("재고를 찾을 수 없습니다. 상품 ID: %d", reservation.getProductId())
                ));

        // 재고 확정 처리(예약 재고 → 실제 재고 차감)
        inventory.confirmReservation(reservation.getQuantity());
        inventoryRepository.save(inventory);

        // 예약 확정(실재고 감소, 예약재고 감소)
        reservation.confirm(orderId);
        reservationRepository.save(reservation);
    }

    /**
     *  예약 취소(심플락 적용) - 주문 실패, 결제 실패 시
     */
    @Transactional(readOnly = true)
    public void cancelReservations(List<InventoryReservation> reservations) {
        for (InventoryReservation reservation : reservations) {
            String lockKey = String.format("inventory:%d:modify", reservation.getProductId());

            simpleLockManager.executeWithSimpleLock(
                    lockKey,
                    LOCK_WAIT_TIME_MS,
                    LOCK_LEASE_TIME_MS,
                    () -> {
                        selfProvider.getObject().cancelReservationInternal(reservation);
                        return null;
                    }
            );


        }
    }

    /**
     * 예약 취소 내부 로직
     * @param reservation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelReservationInternal(InventoryReservation reservation) {
        Inventory inventory = inventoryRepository
                .findByProductId(reservation.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("재고를 찾을 수 없습니다. 상품 ID: %d", reservation.getProductId())
                ));

        // 예약 취소 (reserved_quantity 감소, available_quantity 증가)
        inventory.cancelReservation(reservation.getQuantity());
        inventoryRepository.save(inventory);

        // 예약 상태 업데이트
        reservation.cancel();
        reservationRepository.save(reservation);
    }

    /**
     * 4. 만료된 예약 정리 (스케줄러에서 호출) - 심플락 적용
     */
    @Transactional(readOnly = true)
    public int expireReservations() {
        // RESERVED 상태이면서 만료 시간이 지난 예약 조회
        List<InventoryReservation> expiredReservations =
                reservationRepository.findExpiredReservations(LocalDateTime.now());

        int count = 0;
        for (InventoryReservation reservation : expiredReservations) {
            try {
                String lockKey = String.format("inventory:%d:modify", reservation.getProductId());

                simpleLockManager.executeWithSimpleLock(
                        lockKey,
                        LOCK_WAIT_TIME_MS,
                        LOCK_LEASE_TIME_MS,
                        () -> {
                            selfProvider.getObject().expireReservationInternal(reservation);
                            return null;
                        }
                );

                count++;
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "예약 만료 처리 실패. reservationId: %d, error: %s",
                        reservation.getReservationId(), e.getMessage()
                ));
            }
        }

        return count;
    }

    /**
     * 예약 만료 내부 로직
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireReservationInternal(InventoryReservation reservation) {
        Inventory inventory = inventoryRepository.findByProductId(reservation.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("재고를 찾을 수 없습니다. 상품 ID: %d", reservation.getProductId())
                ));

        // 예약 재고 복구
        inventory.cancelReservation(reservation.getQuantity());
        inventoryRepository.save(inventory);

        // 예약 상태 변경
        reservation.expire();
        reservationRepository.save(reservation);
    }

    /**
     *  5. 재고 추가 (입고) - 심플락 적용
     */
    @Transactional(readOnly = true)
    public void supplyRealQuantity(Long productId, Integer quantity) {
        String lockKey = String.format("inventory:%d:modify", productId);

        // ✅ Simple Lock 적용
        simpleLockManager.executeWithSimpleLock(
                lockKey,
                LOCK_WAIT_TIME_MS,
                LOCK_LEASE_TIME_MS,
                () -> {
                    selfProvider.getObject().supplyRealQuantityInternal(productId, quantity);
                    return null;
                }
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void supplyRealQuantityInternal(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다"));

        inventory.supply(quantity);
        inventoryRepository.save(inventory);
    }

    public record ReserveRequest(Long productId, Integer quantity) {}
}
