package kr.hhplus.be.server.common.status;

public enum PaymentStatus {
    UNPAID,         // 미결제
    PAID,           // 결제 완료
    FAILED,         // 결제 실패
    REFUNDED,       // 환불 완료
    PARTIAL_REFUND, // 부분 환불
    PENDING,        // 결제 대기
    COMPLETED,      // 처리 완료
    CANCELLED       // 취소
}
