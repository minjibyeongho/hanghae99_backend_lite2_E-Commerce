package kr.hhplus.be.server.layered.product.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "inventory")
@Getter
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private Integer realQuantity;              // 실제 재고 수량
    @Column(nullable = false)
    private Integer reservedTmpQuantity;      // 임시 예약 재고 수량
    @Column(nullable = false)
    private Integer reservedConfirmQuantity;  // 확정 예약 재고 수량
    @Column(nullable = false)
    private Integer availableQuantity;         // 예약 가능 재고 수량

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;               // 생성일시
    @UpdateTimestamp
    private Timestamp updatedAt;               // 수정일시

    @Version
    private Long version;                       // 낙관적 락

    protected Inventory(){};

    @Builder
    public Inventory(Long productId, Integer realQuantity) {
        this.productId = productId;
        this.realQuantity = realQuantity;
        this.reservedTmpQuantity = 0;
        this.reservedConfirmQuantity = 0;
        this.availableQuantity = realQuantity;
    }

    // 임시 예약
    public void reserveTemporary(Integer quantity) {
        if (this.availableQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다");
        }
        this.reservedTmpQuantity += quantity;
        this.availableQuantity -= quantity;
    }

    // 예약 확정
    public void confirmReservation(Integer quantity) {
        this.reservedTmpQuantity -= quantity;
        this.reservedConfirmQuantity += quantity;
    }

    // 재고 보충
    public void supply(Integer quantity) {
        this.realQuantity += quantity;
        this.availableQuantity += quantity;
    }

    // 판매 완료 (확정 예약 제거)
    public void completeSale(Integer quantity) {
        this.reservedConfirmQuantity -= quantity;
        this.realQuantity -= quantity;
    }
}
