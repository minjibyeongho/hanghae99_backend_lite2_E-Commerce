package kr.hhplus.be.server.infrastructure.order.repository;

import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.infrastructure.order.entity.OrderJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kr.hhplus.be.server.infrastructure.order.mapper.OrderMapper;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity;

        if (order.getOrderId() != null) {
            // 업데이트
            entity = jpaRepository.findById(order.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));
            mapper.updateEntity(order, entity);
        } else {
            // 신규 생성
            entity = mapper.toEntity(order);
        }

        OrderJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return jpaRepository.findById(orderId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return jpaRepository.findByOrderNumber(orderNumber)
                .map(mapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByUserIdOrderByOrderedAtDesc(Long userId) {
        return jpaRepository.findByUserIdOrderByOrderedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
