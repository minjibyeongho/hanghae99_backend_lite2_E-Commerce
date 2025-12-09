package kr.hhplus.be.server.common.status;

public enum ReservationType {
    ORDER,          // 일반 주문 예약
    CART,           // 장바구니 임시 예약 (선택 사항)
    PROMOTION,      // 프로모션 예약 (선착순 이벤트 등)
    ADMIN_LOCK      // 관리자 잠금 (재고 조정 등)
}
