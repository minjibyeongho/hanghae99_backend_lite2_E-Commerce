package kr.hhplus.be.server.external;

import kr.hhplus.be.server.layered.order.model.Order;
import kr.hhplus.be.server.layered.order.model.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class DataPlatformClient {
    /**
     * 외부 데이터 플랫폼에 주문 정보 전송 (비동기)
     */
    @Async
    public CompletableFuture<Void> sendOrderData(Order order, List<OrderItem> orderItems) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("📤 [데이터 플랫폼] 전송 시작: 주문번호={}", order.getOrderNumber());
                log.info("   - 주문 상품 수: {}", orderItems.size());

                // Mock: 외부 API 호출 시뮬레이션
                Thread.sleep(100);

                log.info("✅ [데이터 플랫폼] 전송 완료: 주문번호={}, 금액={}",
                        order.getOrderNumber(), order.getPaymentAmount());

            } catch (Exception e) {
                log.error("❌ [데이터 플랫폼] 전송 실패: 주문번호={}, 오류={}",
                        order.getOrderNumber(), e.getMessage());
            }
        });
    }
}
