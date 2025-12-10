package kr.hhplus.be.server.domain.order.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.DeliveryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
public class OrderDelivery {

    private Long deliveryId;
    private Long orderId;
    private String deliveryCompany;
    private DeliveryStatus deliveryStatus;
    private String carrier;
    private String carrierPhone;
    private String address1;
    private String address2;
    private LocalDateTime expectedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;

    @Builder
    public OrderDelivery(
        Long deliveryId,
        Long orderId, String deliveryCompany, DeliveryStatus deliveryStatus,
        String carrier, String carrierPhone, String address1, String address2,
        LocalDateTime expectedAt, LocalDateTime deliveredAt, LocalDateTime createdAt
    ) {
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
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // 배송 완료
    public void complete() {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    // 배송 완료 상태 확인
    public boolean isDelivered() {
        return DeliveryStatus.DELIVERED == this.deliveryStatus;
    }
}
