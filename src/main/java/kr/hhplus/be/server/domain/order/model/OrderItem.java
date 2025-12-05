package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
public class OrderItem {

    // JPA 숙달 후에 @ManyToOne, @JoinColumn 어노테이션 이해 및 활용
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", nullable = false)
//    private Order order;

    private Long orderItemId;       // 주문 상세 고유 식별자
    private Long orderId;           // 주문 식별자
    private Long productId;         // 상품 식별자
    private Long reservationId;     // 예약 식별자
    private String productName;     // 상품명
    private Integer quantity;       // 주문 수량
    private Integer unitPrice;      // 주문 당시 단가
    private Integer totalPrice;     // 주문 당시 총가격
    private Timestamp createdAt;    // 등록일시

    protected OrderItem() {}

    @Builder
    public OrderItem(
            Long orderItemId,
            Long orderId, Long productId, Long reservationId, String productName,
                     Integer quantity, Integer unitPrice, Integer totalPrice,
            Timestamp createdAt
    ) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.reservationId = reservationId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public void calculateTotalPrice() {
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public boolean isValidQuantity() {
        return this.quantity != null && this.quantity > 0;
    }

}
