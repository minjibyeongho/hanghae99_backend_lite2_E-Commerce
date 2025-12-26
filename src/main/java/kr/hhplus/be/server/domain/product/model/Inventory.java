package kr.hhplus.be.server.domain.product.model;

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
    private Integer reservedQuantity;          // 예약 재고 수량
    @Column(nullable = false)
    private Integer availableQuantity;         // 예약 가능 재고 수량

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;               // 생성일시
    @UpdateTimestamp
    private Timestamp updatedAt;               // 수정일시

    // @Version
    // private Long version;                       // 낙관적 락 -> 제거(분산락 적용)

    protected Inventory(){};

    @Builder
    public Inventory(Long productId, Integer realQuantity) {
        this.productId = productId;
        this.realQuantity = realQuantity;
        this.reservedQuantity = 0;
        this.availableQuantity = realQuantity;
    }

    // 테스트용 추가
    public static Inventory withAllQuantities(Long productId, Integer realQuantity, Integer reservedQuantity, Integer availableQuantity) {
        Inventory inventory = new Inventory(productId, realQuantity);
        inventory.availableQuantity = availableQuantity;
        inventory.reservedQuantity = reservedQuantity;

        return inventory;
    }

    // 재고 예약(주문 생성 시)
    public void reserve(Integer quantity) {
        // 가용 재고 확인
        if (this.availableQuantity < quantity) {
            throw new IllegalStateException(
                    String.format("재고가 부족합니다. [상품ID: %d] 요청: %d개, 가용: %d개",
                            this.productId, quantity, this.availableQuantity)
            );
        }

        this.reservedQuantity += quantity;
        this.availableQuantity -= quantity;
    }

    // 예약 확정(+ 재고 차감)
    public void confirmReservation(Integer quantity) {
        // 예약 수량 확인
        if (this.reservedQuantity < quantity) {
            throw new IllegalStateException(
                    String.format("예약 수량이 부족합니다. [상품ID: %d] 요청: %d개, 예약: %d개",
                            this.productId, quantity, this.reservedQuantity)
            );
        }

        this.reservedQuantity -= quantity;
        this.realQuantity -= quantity;
        this.availableQuantity = this.realQuantity - this.reservedQuantity;
    }

    // 재고 보충
    public void supply(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("입고 수량은 0보다 커야 합니다");
        }

        this.realQuantity += quantity;
        this.availableQuantity += quantity;
    }

    // 예약 취소 - 추가
    public void cancelReservation(Integer quantity) {
        // 예약 수량 확인
        if (this.reservedQuantity < quantity) {
            throw new IllegalStateException(
                    String.format("취소할 예약 수량이 부족합니다. [상품ID: %d] 요청: %d개, 예약: %d개",
                            this.productId, quantity, this.reservedQuantity)
            );
        }

        this.reservedQuantity -= quantity;
        this.availableQuantity = this.realQuantity - this.reservedQuantity;
    }

    // 가용 재고 조회
    public boolean isAvailable(Integer quantity) {
        return this.availableQuantity >= quantity;
    }
}
