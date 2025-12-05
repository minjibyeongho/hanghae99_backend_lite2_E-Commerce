package kr.hhplus.be.server.infrastructure.sale.repository;

import kr.hhplus.be.server.domain.sale.model.Sale;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleJpaRepository extends JpaRepository<Sale, Long> {
    @Query("SELECT s.productId as productId, " +
            "s.productName as productName, " +
            "SUM(s.soldQuantity) as totalSold, " +
            "SUM(s.soldTotalAmount) as totalRevenue " +
            "FROM Sale s " +
            "WHERE s.soldAt >= :startDate " +
            "GROUP BY s.productId, s.productName " +
            "ORDER BY totalSold DESC")
    List<TopProductProjection> findTopProductsSince(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    interface TopProductProjection {
        Long getProductId();
        String getProductName();
        Long getTotalSold();
        Long getTotalRevenue();
    }
}
