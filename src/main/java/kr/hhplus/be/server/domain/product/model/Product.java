package kr.hhplus.be.server.domain.product.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "product")
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;        // 상품 ID

    @Column(nullable = false)
    private String productName;    // 상품명

    @Column(nullable = false)
    private Long categoryId;       // 카테고리 ID

    @Column(nullable = false)
    private Integer price;          // 상품 가격

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;   // 상품 생성일시

    @UpdateTimestamp
    private Timestamp updatedAt;   // 상품 수정일시


    // 상품은 기본 crud가 있으면 될 것
    protected Product() {}

    @Builder
    public Product(Long categoryId, String productName, Integer price) {
        this.categoryId = categoryId;
        this.productName = productName;
        this.price = price;
    }

}
