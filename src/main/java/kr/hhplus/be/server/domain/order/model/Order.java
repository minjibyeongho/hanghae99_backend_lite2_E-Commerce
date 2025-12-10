package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.OrderStatus;
import kr.hhplus.be.server.common.status.PaymentMethod;
import kr.hhplus.be.server.common.status.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
public class Order {

    private Long orderId;                   // 주문 고유 식별자
    private String orderNumber;             // 사용자 편의성을 위한 주문 번호(ORD-YYYYMMDDhhmmss로 생성)
    private Long userId;                    // 주문자
    private Integer totalAmount;            // 총 주문 금액
    private Integer paymentAmount;          // 결제 금액
    private PaymentMethod paymentMethod;    // 결제 수단
    private OrderStatus orderStatus;        // 주문 상태 코드
    private PaymentStatus paymentStatus;    // 결제 상태
    private Timestamp orderedAt;            // 주문일시
    private Timestamp paidAt;               // 결제일시
    private Timestamp cancelledAt;          // 취소 일시

    /*
    private LocalDateTime orderedAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    */

    // JPA 숙달 후 활용( JPQL 방식 채용해서 활용 )
//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<OrderItem> orderItems = new ArrayList<>();

    // 주문은 주문 관련된 것만( 주문 물품 목록 이런거 다 없이 )

    @Builder
    public Order(
        Long orderId, String orderNumber, Long userId,
        Integer totalAmount, Integer paymentAmount,
        PaymentMethod paymentMethod, OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        Timestamp orderedAt,
        Timestamp paidAt,
        Timestamp cancelledAt
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
        /*
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.paymentAmount = paymentAmount;
        this.paymentMethod = paymentMethod;
        this.orderStatus = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.UNPAID;
         */
    }

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

    // 결제 여부
    public boolean isPaid() {
        return PaymentStatus.PAID == this.paymentStatus;
    }

    // 취소가능 여부
    public boolean isCancellable() {
        return this.orderStatus != OrderStatus.SHIPPED &&
                this.orderStatus != OrderStatus.DELIVERED;
    }
}
