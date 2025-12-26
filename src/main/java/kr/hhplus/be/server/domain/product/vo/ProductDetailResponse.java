package kr.hhplus.be.server.domain.product.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private Integer price;
    private Integer availableQuantity;

}
