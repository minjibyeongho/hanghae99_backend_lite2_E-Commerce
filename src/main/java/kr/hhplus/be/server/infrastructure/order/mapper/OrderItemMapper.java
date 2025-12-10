package kr.hhplus.be.server.infrastructure.order.mapper;

import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.infrastructure.order.entity.OrderItemJpaEntity;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderItemMapper {

    public OrderItem toDomain(OrderItemJpaEntity entity) {
        if (entity == null) return null;

        return OrderItem.builder()
                .orderItemId(entity.getOrderItemId())
                .orderId(entity.getOrderId())
                .productId(entity.getProductId())
                .reservationId(entity.getReservationId())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalPrice(entity.getTotalPrice())
                .createdAt(entity.getCreatedAt() != null ? Timestamp.valueOf(entity.getCreatedAt().toLocalDateTime()) : null)
                .build();
    }

    public OrderItemJpaEntity toEntity(OrderItem domain) {
        if (domain == null) return null;

        return OrderItemJpaEntity.builder()
                .orderItemId(domain.getOrderItemId())
                .orderId(domain.getOrderId())
                .productId(domain.getProductId())
                .reservationId(domain.getReservationId())
                .productName(domain.getProductName())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .totalPrice(domain.getTotalPrice())
                .createdAt(domain.getCreatedAt() != null ? Timestamp.valueOf(domain.getCreatedAt().toLocalDateTime()) : null)
                .build();
    }

    public List<OrderItem> toDomainList(List<OrderItemJpaEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<OrderItemJpaEntity> toEntityList(List<OrderItem> domains) {
        return domains.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
