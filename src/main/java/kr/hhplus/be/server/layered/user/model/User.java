package kr.hhplus.be.server.layered.user.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.layered.wallet.model.Wallet;
import lombok.Getter;

import java.sql.Timestamp;

@Entity
@Table(name = "`user`")
@Getter
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String user_id;

    private String user_name;
    private String user_email;
    private String user_password;
    private String user_phone;
    private Timestamp created_at;
    private Timestamp updated_at;

    @OneToOne(mappedBy = "user")
    Wallet wallet;
}
