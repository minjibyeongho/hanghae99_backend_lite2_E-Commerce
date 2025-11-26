package kr.hhplus.be.server.layered.order;

import kr.hhplus.be.server.layered.order.service.OrderService;
import kr.hhplus.be.server.layered.order.vo.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /*
        주문/결제
     */
    public ResponseEntity<?> orderPayment(@RequestBody OrderRequest orderRequest) {

        // 1. 주문 정보 생성

        // 2.

        return ResponseEntity.ok().build();
    }


}
