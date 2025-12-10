package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.common.status.PaymentMethod;
import kr.hhplus.be.server.common.status.PaymentStatus;
import kr.hhplus.be.server.domain.coupon.core.port.in.UseCouponCommand;
import kr.hhplus.be.server.domain.coupon.core.usecase.UseCouponUseCase;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.external.DataPlatformClient;
import kr.hhplus.be.server.domain.order.model.Order;
import kr.hhplus.be.server.domain.order.model.OrderItem;
import kr.hhplus.be.server.domain.payment.model.Payment;
import kr.hhplus.be.server.infrastructure.payment.repository.PaymentJpaRepository;
import kr.hhplus.be.server.domain.product.model.InventoryReservation;
import kr.hhplus.be.server.domain.product.model.Product;
import kr.hhplus.be.server.infrastructure.product.repository.ProductJpaRepository;
import kr.hhplus.be.server.domain.product.service.InventoryService;
import kr.hhplus.be.server.domain.sale.service.SaleService;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import kr.hhplus.be.server.domain.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    // Clean한 Infrastructure 방식으로 변경
    // 레이어드 패턴에서는 Jpa entity 자체였다면 인터페이스를 바라보게 변경(결합성 낮아짐)
    //private final OrderJpaRepository orderRepository;
    private final OrderRepository orderRepository;  // 인터페이스
    //private final OrderItemJpaRepository orderItemJpaRepository;
    private final OrderItemRepository orderItemRepository;  // 인터페이스

    private final PaymentJpaRepository paymentRepository;
    private final ProductJpaRepository productRepository;

    private final InventoryService inventoryService;
    private final WalletService walletService;
    private final SaleService saleService;
    private final DataPlatformClient dataPlatformClient;

    // clean 아키텍쳐 의존으로 변경
    private final UseCouponUseCase useCouponUseCase;

    /**
     * 주문 및 결제 처리
     */
    @Transactional
    public OrderResponse createOrder(Long userId, List<OrderItemRequest> items, Long couponId) {
        try {
            // 1. 재고 예약
            List<InventoryService.ReserveRequest> reserveRequests = items.stream()
                    .map(item -> new InventoryService.ReserveRequest(item.productId(), item.quantity()))
                    .toList();

            List<InventoryReservation> reservations = inventoryService.reserveInventory(userId, reserveRequests);

            // 2. 주문 생성
            String orderNumber = generateOrderNumber();
            Order order = Order.builder()
                    .orderNumber(orderNumber)
                    .userId(userId)
                    .totalAmount(0)
                    .paymentAmount(0)
                    .paymentMethod(PaymentMethod.WALLET)
                    .build();

            order = orderRepository.save(order);

            // 3. 주문 상품 생성 및 금액 계산
            Integer totalAmount = 0;
            List<OrderItem> orderItems = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                OrderItemRequest itemRequest = items.get(i);
                InventoryReservation reservation = reservations.get(i);

                Product product = productRepository.findById(itemRequest.productId())
                        .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다"));

                Integer unitPrice = product.getPrice();
                Integer itemTotalPrice = unitPrice * itemRequest.quantity();

                OrderItem orderItem = OrderItem.builder()
                        .productId(product.getProductId())
                        .reservationId(reservation.getReservationId())
                        .productName(product.getProductName())
                        .quantity(itemRequest.quantity())
                        .unitPrice(unitPrice)
                        .totalPrice(itemTotalPrice)
                        .build();

                orderItems.add(orderItemRepository.save(orderItem));
                totalAmount += itemTotalPrice;
            }

            // 4. 쿠폰 할인 적용
            Integer discount = 0;
            if (couponId != null) {
                // discount = couponService.calculateDiscount(userId, couponId, totalAmount);
                discount = useCouponUseCase.calculateDiscount(userId, couponId, totalAmount);
            }

            Integer paymentAmount = totalAmount - discount;

            // 5. Order 금액 업데이트
            order.updateAmounts(totalAmount, paymentAmount);
            order.waitForPayment();
            order = orderRepository.save(order);

            // 6. 결제 처리 (Wallet 차감)
            WalletHistory walletHistory = walletService.processPayment(userId, paymentAmount);

            Payment payment = Payment.builder()
                    .orderId(order.getOrderId())
                    .walletHisId(walletHistory.getWalletHisId())
                    .paymentMethod(PaymentMethod.WALLET)
                    .amount(paymentAmount)
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();

            payment.complete();
            paymentRepository.save(payment);

            // 7. 주문 결제 완료
            order.completePayment();

            // 8. 재고 확정
            inventoryService.confirmReservations(reservations, order.getOrderId());

            // 9. 쿠폰 사용 처리
            if (couponId != null) {
                // couponService.useCoupon(userId, couponId, order.getOrderId());
                UseCouponCommand command = new UseCouponCommand(userId, couponId, order.getOrderId());
                useCouponUseCase.execute(command);
            }

            // 10. 판매 기록 생성
            saleService.recordSales(order, orderItems);

            // 11. 외부 데이터 플랫폼 전송 (비동기)
            dataPlatformClient.sendOrderData(order, orderItems);

            return new OrderResponse(
                    order.getOrderId(),
                    order.getOrderNumber(),
                    order.getOrderStatus().name(),
                    paymentAmount
            );

        } catch (Exception e) {
            throw new RuntimeException("주문 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD" + timestamp + uuid;
    }

    // DTO
    public record OrderItemRequest(Long productId, Integer quantity) {}
    public record OrderResponse(Long orderId, String orderNumber, String orderStatus, Integer paymentAmount) {}
}
