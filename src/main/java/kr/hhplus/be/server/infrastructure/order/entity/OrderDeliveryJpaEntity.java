package kr.hhplus.be.server.infrastructure.order.entity;

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
public class OrderDeliveryJpaEntity {

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
    public OrderDeliveryJpaEntity(Long deliveryId, Long orderId, String deliveryCompany,
                                  DeliveryStatus deliveryStatus, String carrier, String carrierPhone,
                                  String address1, String address2, LocalDateTime expectedAt,
                                  LocalDateTime deliveredAt, LocalDateTime createdAt) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.deliveryCompany = deliveryCompany;
        this.deliveryStatus = deliveryStatus;
        this.carrier = carrier;
        this.carrierPhone = carrierPhone;
        this.address1 = address1;
        this.address2 = address2;
        this.expectedAt = expectedAt;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
/*
    비즈니스 메서드 -> domain에서 처리
    // 배송 완료
    public void complete() {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
 */
}
