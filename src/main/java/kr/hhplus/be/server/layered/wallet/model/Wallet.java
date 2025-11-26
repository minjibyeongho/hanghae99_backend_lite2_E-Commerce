package kr.hhplus.be.server.layered.wallet.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "`wallet`")
@Getter
public class Wallet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wallet_id;

    @JoinColumn(name = "user_id")
    private Long user_id;

    private String wallet_name;
    private Integer balance;
    private Timestamp created_at;
    private Timestamp updated_at;

    public void addAmount(int amount){
        if(amount < 0)
            throw new IllegalArgumentException("충전 금액은 음수일 수 없습니다.");

        this.balance += amount;
        this.updated_at = Timestamp.valueOf(LocalDateTime.now());
    }

    public void substractAmount(int amount){
        if(this.balance < amount)
            throw new IllegalArgumentException("잔액이 부족합니다(차감액 > 잔액).");

        this.balance -= amount;
        this.updated_at = Timestamp.valueOf(LocalDateTime.now());
    }

}
