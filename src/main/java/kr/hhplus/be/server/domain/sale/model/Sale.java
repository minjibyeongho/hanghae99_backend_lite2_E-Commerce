package kr.hhplus.be.server.domain.sale.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "sale")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer soldQuantity;

    @Column(nullable = false)
    private Integer soldTotalAmount;

    @Column(nullable = false)
    private Timestamp soldAt;

    private Long categoryId;

    private String categoryName;

    @Builder
    public Sale(Long productId, Long orderId, String productName, Integer soldQuantity,
                Integer soldTotalAmount, Timestamp soldAt, Long categoryId, String categoryName) {
        this.productId = productId;
        this.orderId = orderId;
        this.productName = productName;
        this.soldQuantity = soldQuantity;
        this.soldTotalAmount = soldTotalAmount;
        this.soldAt = soldAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
}
