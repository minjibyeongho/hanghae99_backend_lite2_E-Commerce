package kr.hhplus.be.server.domain.wallet.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import kr.hhplus.be.server.common.status.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_history")
@Getter
public class WalletHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletHisId;

    @Column(nullable = false)
    private Long walletId;

    @Column(nullable = false)
    private Integer amount;
    @Column(nullable = false)
    private Integer beforeBalance;
    @Column(nullable = false)
    private Integer afterBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletHistoryStatus status;

    private String memo;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 멱등성 키 (UNIQUE 제약)
    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    // jpa 생성자
    protected WalletHistory() {};

    @Builder
    public WalletHistory(Long walletId, Integer amount, Integer beforeBalance,
                         Integer afterBalance, WalletHistoryStatus status, String memo, String idempotencyKey) {
        this.walletId = walletId;
        this.amount = amount;
        this.beforeBalance = beforeBalance;
        this.afterBalance = afterBalance;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.memo = memo;
    }

    // 지갑 이력 생성
    public static WalletHistory createChargeHistory(
            Wallet wallet, Integer amount, Integer beforeBalance
    ) {
        return WalletHistory.builder()
                .walletId(wallet.getWalletId())
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(wallet.getBalance())
                .status(WalletHistoryStatus.CHARGE)
                .memo("잔액 충전")
                .build();
    }

    // 지갑 결제 생성
    public static WalletHistory createPaymentHistory(Wallet wallet, Integer amount, Integer beforeBalance) {
        return WalletHistory.builder()
                .walletId(wallet.getWalletId())
                .amount(-amount)
                .beforeBalance(beforeBalance)
                .afterBalance(wallet.getBalance())
                .status(WalletHistoryStatus.PAYMENT)
                .memo("결제 차감")
                .build();
    }

    /**
     * 잔액 변동량 계산
     * @return afterBalance - beforeBalance
     */
    public Integer getBalanceChange() {
        return this.afterBalance - this.beforeBalance;
    }

    /**
     * 잔액 증가 여부
     */
    public boolean isBalanceIncreased() {
        return this.afterBalance > this.beforeBalance;
    }

    /**
     * 잔액 감소 여부
     */
    public boolean isBalanceDecreased() {
        return this.afterBalance < this.beforeBalance;
    }

    /**
     * 충전 이력 여부
     */
    public boolean isCharge() {
        return this.status == WalletHistoryStatus.CHARGE;
    }

    /**
     * 결제 이력 여부
     */
    public boolean isPayment() {
        return this.status == WalletHistoryStatus.PAYMENT;
    }
}
