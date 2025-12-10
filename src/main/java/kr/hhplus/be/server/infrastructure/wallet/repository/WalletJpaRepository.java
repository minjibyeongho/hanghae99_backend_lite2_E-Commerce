package kr.hhplus.be.server.infrastructure.wallet.repository;

import kr.hhplus.be.server.domain.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {

    /**
     * 사용자 ID로 지갑 조회
     * @param userId
     * @return
     */
    Optional<Wallet> findByUserId(Long userId);

    /**
     * 잔액이 충분한 경우에만 차감 (조건부 업데이트)
     * @param userId 사용자 ID
     * @param amount 차감할 금액
     * @return 업데이트된 행 수 (0 = 실패, 1 = 성공)
     *
     * 동작 방식:
     * 1. WHERE 절에서 balance >= amount 조건 확인
     * 2. 조건이 참이면 UPDATE 실행, 거짓이면 0 반환
     * 3. DB가 원자적(atomic)으로 처리하여 동시성 안전
     */
    @Modifying(clearAutomatically = true)  // UPDATE/DELETE 쿼리 표시
    @Query("UPDATE Wallet w " +
            "SET w.balance = w.balance - :amount, " +
            "    w.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE w.userId = :userId " +
            "AND w.balance >= :amount")  // ← 핵심: 조건부 실행
    int deductBalanceIfSufficient(
            @Param("userId") Long userId,
            @Param("amount") Integer amount
    );

    /**
     * 잔액 충전 (무조건 실행)
     *
     * @param userId 사용자 ID
     * @param amount 충전할 금액
     * @return 업데이트된 행 수 (0 = 실패, 1 = 성공)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Wallet w " +
            "SET w.balance = w.balance + :amount, " +
            "    w.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE w.userId = :userId")
    int chargeBalance(
            @Param("userId") Long userId,
            @Param("amount") Integer amount
    );
}
