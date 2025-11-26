package kr.hhplus.be.server.layered.sale.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Entity
@Table(name = "sale")
public class Sale {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sale_id;

    @JoinColumn(name = "product_id")
    private Long product_id;

    @JoinColumn(name = "order_id")
    private Long order_id;

    private String product_name;

    @Column(nullable = false)
    private Integer sold_quantity;
    private Integer sold_total_amount;

    private Timestamp sold_at;
    private Long category_id;
    private String category_name;

    protected Sale() {};

    private Sale(String product_name, Integer sold_quantity, Integer sold_total_amount, Timestamp sold_at, Long category_id, String category_name) {
        this.product_name = product_name;
        this.sold_quantity = sold_quantity;
        this.sold_total_amount = sold_total_amount;
        this.sold_at = sold_at;
        this.category_id = category_id;
        this.category_name = category_name;
    }

    public static Sale create(String product_name, Integer sold_quantity, Integer sold_total_amount, Timestamp sold_at, Long category_id, String category_name){
        return new Sale(product_name, sold_quantity, sold_total_amount, sold_at, category_id, category_name);
    }

}
