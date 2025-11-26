package kr.hhplus.be.server.layered.order.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.status.OrderStatus;
import kr.hhplus.be.server.common.status.PaymentStatus;
import lombok.Getter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity
@Table(name = "`order`")
@Getter
public class Order {

    // 주문 고유 식별자
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long order_id;

    @Column(nullable = false)
    private String order_number;    // 사용자 편의성을 위한 주문 번호(ORD-YYYYMMDDhhmmss로 생성)

    // 주문자
    @Column(nullable = false)
    private Long user_id;

    // 총 주문 금액
    @Column(nullable = false)
    private Long total_amount;

    // 결제 금액
    private Long payment_amount;

    // 결제 수단
    private String payment_method;

    // 주문 상태 코드
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus order_status;

    // 결제 상태
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus payment_status;

    // 주문일시
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp ordered_at;

    // 결제일시
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp paid_at;

    // 취소 일시
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp cancelled_at;

    // 주문은 주문 관련된 것만( 주문 물품 목록 이런거 다 없이 )
    // jpa를 위한 생성자
    protected Order() {}

    // 첫 주문 시 주문 객체 생성
    private Order(Long user_id) {
        Date today = new Date();
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", currentLocale);

        this.user_id = user_id;
        this.order_number = "ORD-" + formatter.format(today);   // ORD-20251122153422
        this.order_status = OrderStatus.PENDING;
    }

    /*
    Date today = new Date();
		Locale currentLocale = new Locale("KOREAN", "KOREA");
		String pattern = "yyyyMMddHHmmss"; //hhmmss로 시간,분,초만 뽑기도 가능
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, currentLocale);
		System.out.println(formatter.format(today));
     */

    // 주문 생성 메소드
    public static Order create(Long user_id) {
        return new Order(user_id);
    }


}
