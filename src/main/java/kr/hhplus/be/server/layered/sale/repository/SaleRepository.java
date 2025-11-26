package kr.hhplus.be.server.layered.sale.repository;

import kr.hhplus.be.server.layered.sale.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
