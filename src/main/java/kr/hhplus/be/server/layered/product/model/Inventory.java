package kr.hhplus.be.server.layered.product.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;

@Entity
@Table(name = "inventory")
@Getter
public class Inventory {

    @Id @GeneratedValue
    private Long inventory_id;

    @JoinColumn(name = "product_id")
    private Long product_id;

    private Integer real_quantity;              // 실제 재고 수량
    private Integer reserved_tmp_quantity;      // 임시 예약 재고 수량
    private Integer reserved_confirm_quantity;  // 확정 예약 재고 수량
    private Integer available_quantity;         // 예약 가능 재고 수량
    private Timestamp created_at;               // 생성일시
    private Timestamp updated_at;               // 수정일시
    private Long version;                       // 낙관적 락

    protected Inventory(){};


    /*
        inventory_id BIGINT [primary key, note: '재고 식별자']
        product_id BIGINT [note: '상품 식별자']
        real_quantity Integer [note: '실제 재고 수량']
        reserved_tmp_quantity Integer [note: '임시 예약 재고 수량']
        reserved_confirm_quantity Integer [note: '확정 예약 재고 수량']
        available_quantity Integer [note: '예약 가능 재고 수량']
        created_at timestamp [note: '생성일시']
        updated_at timestamp [note: '수정일시']
        version BIGINT [note: '낙관적 락']
     */
}
