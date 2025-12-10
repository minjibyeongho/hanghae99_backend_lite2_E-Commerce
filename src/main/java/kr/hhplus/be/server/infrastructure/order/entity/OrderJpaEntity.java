package kr.hhplus.be.server.infrastructure.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.OrderStatus;
import kr.hhplus.be.server.common.status.PaymentMethod;
import kr.hhplus.be.server.common.status.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
public class OrderJpaEntity {

    // 주문 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false, unique = true)
    private String orderNumber;    // 사용자 편의성을 위한 주문 번호(ORD-YYYYMMDDhhmmss로 생성)

    // 주문자
    @Column(nullable = false)
    private Long userId;

    // 총 주문 금액
    @Column(nullable = false)
    private Integer totalAmount;

    // 결제 금액
    private Integer paymentAmount;

    // 결제 수단
    private PaymentMethod paymentMethod;

    // 주문 상태 코드
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    // 결제 상태
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // 주문일시
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp orderedAt;

    // 결제일시
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp paidAt;

    // 취소 일시
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp cancelledAt;

    // JPA 숙달 후 활용( JPQL 방식 채용해서 활용 )
//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<OrderItem> orderItems = new ArrayList<>();

    // 주문은 주문 관련된 것만( 주문 물품 목록 이런거 다 없이 )
    // jpa를 위한 생성자
    protected OrderJpaEntity() {}

    @Builder
    public OrderJpaEntity(
        Long orderId,
        String orderNumber, Long userId, Integer totalAmount,
        Integer paymentAmount, PaymentMethod paymentMethod,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus, Timestamp orderedAt,
        Timestamp paidAt, Timestamp cancelledAt
    ) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.paymentAmount = paymentAmount;
        this.paymentMethod = paymentMethod;
        this.orderStatus = orderStatus != null ? orderStatus : OrderStatus.PENDING;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.UNPAID;
        this.orderedAt = orderedAt != null ? orderedAt : Timestamp.valueOf(LocalDateTime.now());
        this.paidAt = paidAt;
        this.cancelledAt = cancelledAt;
    }

    // JPA용 Setter (Mapper에서만 사용) -> 명시적으로 작성( 교육용 )
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setPaymentAmount(Integer paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

/*
    비즈니즈 메서드는 domain에서 처리
    infrastructure - entity는 외부(여기서는 DB)와의 소통만을 담당

    // 결제 대기
    public void waitForPayment() {
        if (OrderStatus.PENDING != this.orderStatus) {
            throw new IllegalStateException("결제 대기 상태로 변경할 수 없습니다.");
        }
        this.orderStatus = OrderStatus.PAYMENT_WAITING;
    }

    // 결제 완료
    public void completePayment() {
        if (OrderStatus.PAYMENT_WAITING != this.orderStatus) {
            throw new IllegalStateException("결제를 완료할 수 없는 상태입니다.");
        }
        this.orderStatus = OrderStatus.PAYMENT_COMPLETED;
        this.paymentStatus = PaymentStatus.PAID;
        this.paidAt = Timestamp.valueOf(LocalDateTime.now());
    }

    // 주문 취소
    public void cancel() {
        if ( (OrderStatus.SHIPPED == this.orderStatus) || (OrderStatus.DELIVERED == this.orderStatus) ) {
            throw new IllegalStateException("배송 중이거나 완료된 주문은 취소할 수 없습니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledAt = Timestamp.valueOf(LocalDateTime.now());
    }

    // 금액 업데이트 메서드 추가
    public void updateAmounts(Integer totalAmount, Integer paymentAmount) {
        this.totalAmount = totalAmount;
        this.paymentAmount = paymentAmount;
    }
 */
}
