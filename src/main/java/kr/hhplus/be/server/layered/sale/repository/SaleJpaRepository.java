package kr.hhplus.be.server.layered.sale.repository;

import kr.hhplus.be.server.layered.sale.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
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
