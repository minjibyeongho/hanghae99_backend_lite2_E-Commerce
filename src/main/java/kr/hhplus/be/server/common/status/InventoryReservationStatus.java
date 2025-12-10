package kr.hhplus.be.server.common.status;

public enum InventoryReservationStatus {
    RESERVED,    // 예약됨 (재고에서 차감 대기)
    CONFIRMED,   // 확정됨 (주문 완료, 실제 재고 차감)
    CANCELLED,   // 취소됨 (예약 취소, 재고 복구)
    EXPIRED      // 만료됨 (시간 초과, 자동 취소)
}

