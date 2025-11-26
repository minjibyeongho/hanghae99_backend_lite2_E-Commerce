package kr.hhplus.be.server.layered.wallet.model;

import jakarta.persistence.*;
import lombok.Getter;
import kr.hhplus.be.server.common.status.*;

@Entity
@Table(name = "wallet_history")
@Getter
public class WalletHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wallet_his_id;

    @JoinColumn(name = "wallet_id")
    private Long wallet_id;

    private Integer amount;
    private Integer before_balance;
    private Integer after_balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletHistoryStatus status;

    private String memo;

    /*
    wallet_his_id BIGINT [primary key, note: '지갑 식별자']
    wallet_id BIGINT
    amount Integer
    before_balance Integer
    after_balance Integer
    status VARCHAR [note: '상태(CHARGE, PAYMENT, REFUND, WITHDRAW']
    memo VARHCAR [note: '비고']
     */
}
