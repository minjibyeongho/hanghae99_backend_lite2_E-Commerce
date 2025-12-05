package kr.hhplus.be.server.domain.wallet.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @Column(nullable = false)
    private Long userId;

    private String walletName;

    @Column(nullable = false)
    private Integer balance;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Builder
    public Wallet(Long userId, String walletName, Integer balance) {
        this.userId = userId;
        this.walletName = walletName;
        this.balance = balance != null ? balance : 0;
    }

    // 테스트용 추가
    public static Wallet withId(Long walletId, Long userId, String walletName, Integer balance) {
        Wallet wallet = new Wallet(userId, walletName, balance);
        wallet.walletId = walletId;  // Reflection 없이 직접 할당
        return wallet;
    }

    /**
     * 금액 충전
     * @return afterBalance(balance+amount)
     */
    public Integer addAmount(Integer amount){
        if(amount == null)
            throw new IllegalArgumentException("충전 금액은 null일 수 없습니다.");

        if(amount <= 0)
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");

        this.balance += amount;
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());

        return this.balance;
    }

    /**
     * 금액 차감(결제)
     * @return afterBalance(balance-amount)
     */
    public Integer substractAmount(Integer amount){

        if(amount <= 0){
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }

        if(this.balance < amount)
            throw new IllegalArgumentException("잔액이 부족합니다(차감액 > 잔액).");

        this.balance -= amount;
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
        return this.balance;
    }

}
