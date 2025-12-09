package kr.hhplus.be.server.domain.product.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.InventoryReservationStatus;
import kr.hhplus.be.server.common.status.ReservationType;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_reservation")
@Getter
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationType reservationType; // ORDER, CART, PROMOTION, ADMIN_LOCK

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InventoryReservationStatus status; // RESERVED, CONFIRMED, CANCELLED, EXPIRED

    private Long orderId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime completedAt;

    protected InventoryReservation() {}

    @Builder
    public InventoryReservation(Long productId, Long userId, Integer quantity, InventoryReservationStatus status,
                                ReservationType reservationType, LocalDateTime expiresAt) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.status = status;
        this.reservationType = reservationType;
        this.status = InventoryReservationStatus.RESERVED;
        this.expiresAt = expiresAt;
    }

    // 예약 확정(주문 완료 시)
    public void confirm(Long orderId) {
        if (!(InventoryReservationStatus.RESERVED==this.status)) {
            throw new IllegalStateException(
                String.format("예약 상태가 RESERVED가 아닙니다. 현재: %s", this.status)
            );
        }

        this.status = InventoryReservationStatus.CONFIRMED;
        this.orderId = orderId;
        this.completedAt = LocalDateTime.now();
    }

    // 예약 취소(주문 실패시)
    public void cancel() {
        if (this.status != InventoryReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    String.format("예약 상태가 RESERVED가 아닙니다. 현재: %s", this.status)
            );
        }
        this.status = InventoryReservationStatus.CANCELLED;
    }

    // 예약 만료 (시간 초과 시)
    public void expire() {
        if (this.status != InventoryReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    String.format("예약 상태가 RESERVED가 아닙니다. 현재: %s", this.status)
            );
        }

        this.status = InventoryReservationStatus.EXPIRED;
    }

    // 예약 만료 체크
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
