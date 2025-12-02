package kr.hhplus.be.server.domain.order.vo;

import java.util.List;

public class OrderRequest {
    private Long user_id;
    private Integer total_amount;
    private Long coupon_id;
    private List<OrderItem> items;
}
