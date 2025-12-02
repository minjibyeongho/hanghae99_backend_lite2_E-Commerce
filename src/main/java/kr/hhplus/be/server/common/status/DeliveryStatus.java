package kr.hhplus.be.server.common.status;

public enum DeliveryStatus {
    PENDING,    // 대기
    PREPARING,  // 준비 중
    SHIPPED,    // 발송 완료
    IN_TRANSIT, // 배송 중
    DELIVERED   // 배송 완료
}
