package kr.hhplus.be.server.infrastructure.order.repository;

import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.infrastructure.order.entity.OrderItemJpaEntity;
import kr.hhplus.be.server.infrastructure.order.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJpaRepository jpaRepository;
    private final OrderItemMapper mapper;

    @Override
    public OrderItem save(OrderItem orderItem) {
        OrderItemJpaEntity entity = mapper.toEntity(orderItem);
        OrderItemJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return mapper.toDomainList(jpaRepository.findByOrderId(orderId));
    }

    @Override
    public List<OrderItem> findByOrderIdIn(List<Long> orderIds) {
        return mapper.toDomainList(jpaRepository.findByOrderIdIn(orderIds));
    }

    @Override
    public List<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId) {
        return mapper.toDomainList(jpaRepository.findByOrderIdAndProductId(orderId, productId));
    }

    @Override
    public List<OrderItem> findByUserIdAndProductId(Long userId, Long productId) {
        return mapper.toDomainList(jpaRepository.findByUserIdAndProductId(userId, productId));
    }

    @Override
    public long countByOrderId(Long orderId) {
        return jpaRepository.countByOrderId(orderId);
    }

    @Override
    public Integer sumTotalPriceByOrderId(Long orderId) {
        return jpaRepository.sumTotalPriceByOrderId(orderId);
    }
}
