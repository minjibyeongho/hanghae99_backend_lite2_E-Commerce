package kr.hhplus.be.server.layered.product.model;

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
    private ReservationType reservationType; // SOFT, HARD

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InventoryReservationStatus status; // ACTIVE, EXPIRED, CANCELLED, COMPLETED

    private Long orderId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime completedAt;

    protected InventoryReservation() {}

    @Builder
    public InventoryReservation(Long productId, Long userId, Integer quantity,
                                ReservationType reservationType, LocalDateTime expiresAt) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.reservationType = reservationType;
        this.status = InventoryReservationStatus.ACTIVE;
        this.expiresAt = expiresAt;
    }

    // 예약 확정
    public void confirm(Long orderId) {
        if (!(InventoryReservationStatus.ACTIVE==this.status)) {
            throw new IllegalStateException("활성 상태의 예약만 확정할 수 있습니다");
        }
        this.status = InventoryReservationStatus.COMPLETED;
        this.orderId = orderId;
        this.completedAt = LocalDateTime.now();
    }

    // 예약 취소
    public void cancel() {
        if ((InventoryReservationStatus.COMPLETED==this.status)) {
            throw new IllegalStateException("완료된 예약은 취소할 수 없습니다");
        }
        this.status = InventoryReservationStatus.CANCELLED;
    }

    // 예약 만료 체크
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
