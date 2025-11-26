package kr.hhplus.be.server.layered.order.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;

@Entity
@Table(name = "order_item")
@Getter
public class OrderItem {

    // 주문 상세 고유 식별자
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long order_item_id;

    // 주문 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // 상품 식별자
    @Column(nullable = false)
    private Long product_id;

    // 예약 식별자
    @Column(nullable = false)
    private Long reservation_id;

    // 상품명
    @Column(nullable = false)
    private Long product_name;

    // 주문 수량
    @Column(nullable = false)
    private Integer quantity;

    // 주문 당시 단가
    @Column(nullable = false)
    private Integer unit_price;

    // 주문 당시 총가격
    @Column(nullable = false)
    private Integer total_price;

    // 등록일시
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp created_at;

    protected OrderItem() {}

    public OrderItem(Order order, Long product_id, Integer quantity, Integer unit_price) {
        this.order = order;
        this.product_id = product_id;
        this.quantity = quantity;
        this.unit_price = unit_price;
    }


}
