package kr.hhplus.be.server.infrastructure.wallet.repository;

import kr.hhplus.be.server.common.status.WalletHistoryStatus;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletHistoryJpaRepository extends JpaRepository<WalletHistory, Long> {

    /**
     * 지갑 ID로 이력 조회 (최신순 정렬)
     * @param walletId
     * @return
     */
    List<WalletHistory> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    /**
     * 특정 기간의 이력 조회
     * @param walletId
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("SELECT wh FROM WalletHistory wh " +
            "WHERE wh.walletId = :walletId " +
            "AND wh.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY wh.createdAt DESC")
    List<WalletHistory> findByWalletIdAndDateRange(
            @Param("walletId") Long walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 상태별 이력 조회
     * @param walletId
     * @param status
     * @return
     */
    List<WalletHistory> findByWalletIdAndStatus(Long walletId, WalletHistoryStatus status);

    /**
     * 멱등성 키로 이력 조회 (중복 결제 확인용)
     *
     * @param idempotencyKey 멱등성 키 (예: payment_ORD20251210001)
     * @return 이미 처리된 이력 (Optional)
     *
     * 동작 방식:
     * 1. idempotency_key 컬럼에서 일치하는 레코드 검색
     * 2. 존재하면 Optional.of(history), 없으면 Optional.empty()
     * 3. UNIQUE 제약으로 최대 1개만 존재
     */
    Optional<WalletHistory> findByIdempotencyKey(String idempotencyKey);

    /**
     * 멱등성 키 존재 여부 확인 (더 빠른 조회)
     *
     * @param idempotencyKey 멱등성 키
     * @return 존재 여부 (true/false)
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * 특정 주문의 결제 이력 조회 (주문 번호 기반)
     * 멱등성 키가 "payment_{orderNumber}" 형식일 때 사용
     */
    @Query("SELECT wh FROM WalletHistory wh " +
            "WHERE wh.idempotencyKey LIKE CONCAT('payment_', :orderNumber)")
    Optional<WalletHistory> findByOrderNumber(@Param("orderNumber") String orderNumber);

}
