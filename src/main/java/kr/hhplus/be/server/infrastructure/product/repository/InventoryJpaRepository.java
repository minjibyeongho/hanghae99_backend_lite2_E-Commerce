package kr.hhplus.be.server.infrastructure.product.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.product.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryJpaRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    // 재고 조회에서 비관적 락
    // LockModeType.PESSIMISTIC_WRITE: 다른 트랜잭션이 읽기/쓰기 모두 차단, SQL의 SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") Long productId);


}