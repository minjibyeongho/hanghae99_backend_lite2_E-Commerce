package kr.hhplus.be.server.domain.sale.vo;

public record TopProductResponse(
        Long productId,
        String productName,
        Long totalSold,
        Long totalRevenue
) {}
