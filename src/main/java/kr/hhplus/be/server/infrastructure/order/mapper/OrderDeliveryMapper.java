package kr.hhplus.be.server.infrastructure.order.mapper;

import kr.hhplus.be.server.domain.order.model.OrderDelivery;
import kr.hhplus.be.server.infrastructure.order.entity.OrderDeliveryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderDeliveryMapper {
    public OrderDelivery toDomain(OrderDeliveryJpaEntity entity) {
        if (entity == null) return null;

        return OrderDelivery.builder()
                .deliveryId(entity.getDeliveryId())
                .orderId(entity.getOrderId())
                .deliveryCompany(entity.getDeliveryCompany())
                .deliveryStatus(entity.getDeliveryStatus())
                .carrier(entity.getCarrier())
                .carrierPhone(entity.getCarrierPhone())
                .address1(entity.getAddress1())
                .address2(entity.getAddress2())
                .expectedAt(entity.getExpectedAt())
                .deliveredAt(entity.getDeliveredAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public OrderDeliveryJpaEntity toEntity(OrderDelivery domain) {
        if (domain == null) return null;

        return OrderDeliveryJpaEntity.builder()
                .deliveryId(domain.getDeliveryId())
                .orderId(domain.getOrderId())
                .deliveryCompany(domain.getDeliveryCompany())
                .deliveryStatus(domain.getDeliveryStatus())
                .carrier(domain.getCarrier())
                .carrierPhone(domain.getCarrierPhone())
                .address1(domain.getAddress1())
                .address2(domain.getAddress2())
                .expectedAt(domain.getExpectedAt())
                .deliveredAt(domain.getDeliveredAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
