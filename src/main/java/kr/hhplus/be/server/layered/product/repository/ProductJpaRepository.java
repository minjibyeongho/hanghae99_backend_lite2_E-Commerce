package kr.hhplus.be.server.layered.product.repository;

import kr.hhplus.be.server.layered.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
