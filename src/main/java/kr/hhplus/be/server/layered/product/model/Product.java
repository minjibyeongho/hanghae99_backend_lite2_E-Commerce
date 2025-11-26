package kr.hhplus.be.server.layered.product.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;

@Entity
@Table(name = "product")
@Getter
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long product_id;        // 상품 ID

    private String product_name;    // 상품명
    private Long category_id;       // 카테고리 ID
    private Integer price;          // 상품 가격
    private Timestamp created_at;   // 상품 생성일시
    private Timestamp updated_at;   // 상품 수정일시


    // 상품은 기본 crud가 있으면 될 것
    protected Product() {}

    private Product(String product_name, Long category_id, Integer price) {
        this.product_name = product_name;
        this.category_id = category_id;
        this.price = price;
    }


    // 상품 등록
        // 필요시
    // 상품 조회
        // 필요시
    // 상품 수정
        // 필요시
}
