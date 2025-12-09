package kr.hhplus.be.server.domain.product.scheduler;

import kr.hhplus.be.server.domain.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryReservationScheduler {

    private final InventoryService inventoryService;

    // 1분마다 만료된 예약 정리
    @Scheduled(fixedRate = 60000)  // 60초
    public void expireReservations() {
        try {
            int expiredCount = inventoryService.expireReservations();

            if (expiredCount > 0) {
                System.out.println(String.format("예약 만료 처리 완료: {}건", expiredCount));
            }

        } catch (Exception e) {
            System.out.println(String.format("예약 만료 처리 중 오류 발생, 메세지: {}", e.getMessage()));
        }
    }

}
