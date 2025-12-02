package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.DeliveryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "order_delivery")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    @Column(nullable = false)
    private Long orderId;

    private String deliveryCompany;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus;

    private String carrier;

    private String carrierPhone;

    private String address1;

    private String address2;

    private LocalDateTime expectedAt;

    private LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public OrderDelivery(Long orderId, String deliveryCompany, DeliveryStatus deliveryStatus,
                         String carrier, String carrierPhone, String address1, String address2,
                         LocalDateTime expectedAt) {
        this.orderId = orderId;
        this.deliveryCompany = deliveryCompany;
        this.deliveryStatus = deliveryStatus;
        this.carrier = carrier;
        this.carrierPhone = carrierPhone;
        this.address1 = address1;
        this.address2 = address2;
        this.expectedAt = expectedAt;
    }

    // 배송 완료
    public void complete() {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
}
