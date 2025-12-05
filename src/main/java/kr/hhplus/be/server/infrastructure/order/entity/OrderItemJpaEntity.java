package kr.hhplus.be.server.infrastructure.order.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "order_item")
@Getter
public class OrderItemJpaEntity {

    // 주문 상세 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    // 주문 식별자
    // JPA 숙달 후에 @ManyToOne, @JoinColumn 어노테이션 이해 및 활용
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", nullable = false)
//    private Order order;

    @Column(nullable = false)
    private Long orderId;

    // 상품 식별자
    @Column(nullable = false)
    private Long productId;

    // 예약 식별자
    @Column(nullable = false)
    private Long reservationId;

    // 상품명
    @Column(nullable = false)
    private String productName;

    // 주문 수량
    @Column(nullable = false)
    private Integer quantity;

    // 주문 당시 단가
    @Column(nullable = false)
    private Integer unitPrice;

    // 주문 당시 총가격
    @Column(nullable = false)
    private Integer totalPrice;

    // 등록일시
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    // 명시적 작성 또는 @NoArgsConstructor(access = AccessLevel.PROTECTED) 어노테이션 활용
    protected OrderItemJpaEntity() {}

    @Builder
    public OrderItemJpaEntity(Long orderItemId, Long orderId, Long productId,
                              Long reservationId, String productName, Integer quantity,
                              Integer unitPrice, Integer totalPrice, Timestamp createdAt) {
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

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

}
