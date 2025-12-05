package kr.hhplus.be.server.infrastructure.order.mapper;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.infrastructure.order.entity.OrderJpaEntity;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class OrderMapper {

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) return null;

        return Order.builder()
                .orderId(entity.getOrderId())
                .orderNumber(entity.getOrderNumber())
                .userId(entity.getUserId())
                .totalAmount(entity.getTotalAmount())
                .paymentAmount(entity.getPaymentAmount())
                .paymentMethod(entity.getPaymentMethod())
                .orderStatus(entity.getOrderStatus())
                .paymentStatus(entity.getPaymentStatus())
                .orderedAt(entity.getOrderedAt() != null ? Timestamp.valueOf(entity.getOrderedAt().toLocalDateTime()) : null)
                .paidAt(entity.getPaidAt() != null ? Timestamp.valueOf(entity.getPaidAt().toLocalDateTime()) : null)
                .cancelledAt(entity.getCancelledAt() != null ? Timestamp.valueOf(entity.getCancelledAt().toLocalDateTime()) : null)
                .build();
    }

    public OrderJpaEntity toEntity(Order domain) {
        if (domain == null) return null;

        return OrderJpaEntity.builder()
                .orderId(domain.getOrderId())
                .orderNumber(domain.getOrderNumber())
                .userId(domain.getUserId())
                .totalAmount(domain.getTotalAmount())
                .paymentAmount(domain.getPaymentAmount())
                .paymentMethod(domain.getPaymentMethod())
                .orderStatus(domain.getOrderStatus())
                .paymentStatus(domain.getPaymentStatus())
                .orderedAt(domain.getOrderedAt() != null ? Timestamp.valueOf(domain.getOrderedAt().toLocalDateTime()) : null)
                .paidAt(domain.getPaidAt() != null ? Timestamp.valueOf(domain.getPaidAt().toLocalDateTime()) : null)
                .cancelledAt(domain.getCancelledAt() != null ? Timestamp.valueOf(domain.getCancelledAt().toLocalDateTime()) : null)
                .build();
    }

    public void updateEntity(Order domain, OrderJpaEntity entity) {
        entity.setOrderStatus(domain.getOrderStatus());
        entity.setPaymentStatus(domain.getPaymentStatus());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setPaymentAmount(domain.getPaymentAmount());
        entity.setPaidAt(domain.getPaidAt() != null ? Timestamp.valueOf(domain.getPaidAt().toLocalDateTime()) : null);
        entity.setCancelledAt(domain.getCancelledAt() != null ? Timestamp.valueOf(domain.getCancelledAt().toLocalDateTime()) : null);
    }
}
