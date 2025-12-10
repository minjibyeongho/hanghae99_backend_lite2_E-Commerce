package kr.hhplus.be.server.infrastructure.product.repository;

import kr.hhplus.be.server.domain.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    // 카테고리별 상품 조회
    List<Product> findByCategoryId(Long categoryId);
}
