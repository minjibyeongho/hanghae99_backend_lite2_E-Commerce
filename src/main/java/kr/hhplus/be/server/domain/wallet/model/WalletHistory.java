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

    // jpa 생성자
    protected WalletHistory() {};

    @Builder
    public WalletHistory(Long walletId, Integer amount, Integer beforeBalance,
                         Integer afterBalance, WalletHistoryStatus status, String memo) {
        this.walletId = walletId;
        this.amount = amount;
        this.beforeBalance = beforeBalance;
        this.afterBalance = afterBalance;
        this.status = status;
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
}
