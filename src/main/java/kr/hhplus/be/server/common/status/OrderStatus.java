package kr.hhplus.be.server.common.status;

public enum OrderStatus {
    PENDING,            // 대기
    PAYMENT_WAITING,    // 결제 대기
    PAYMENT_COMPLETED,  // 결제 완료
    PREPARING,          // 배송 준비
    SHIPPED,            // 배송 중
    DELIVERED,          // 배송 완료
    CANCELLED           // 취소
}
