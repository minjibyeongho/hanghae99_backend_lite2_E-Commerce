package kr.hhplus.be.server.domain.payment.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.PaymentMethod;
import kr.hhplus.be.server.common.status.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false)
    private Long orderId;

    private Long walletHisId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String failedReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Payment() {}

    @Builder
    public Payment(Long orderId, Long walletHisId, PaymentMethod paymentMethod,
                   Integer amount, PaymentStatus paymentStatus) {
        this.orderId = orderId;
        this.walletHisId = walletHisId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    // 결제 완료
    public void complete() {
        if (PaymentStatus.PENDING!=this.paymentStatus) {
            throw new IllegalStateException("결제 완료 처리할 수 없는 상태입니다.");
        }
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }

    // 결제 실패
    public void fail(String reason) {
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.failedReason = reason;
    }
}
