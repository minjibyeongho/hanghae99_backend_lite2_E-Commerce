package kr.hhplus.be.server.layered.user.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.layered.wallet.model.Wallet;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userName;
    private String userEmail;
    private String userPassword;
    private String userPhone;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Builder
    public User(String name, String email, String password, String phone) {
        this.userName = name;
        this.userEmail = email;
        this.userPassword = password;
        this.userPhone = phone;
    }

    // 비밀번호 변경
    public void changePassword(String newPassword) {
        this.userPassword = newPassword;
    }

    // 전화번호 변경
    public void changePhone(String newPhone) {
        this.userPhone = newPhone;
    }
}
