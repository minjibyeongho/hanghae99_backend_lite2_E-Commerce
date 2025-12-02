package kr.hhplus.be.server.layered.wallet.repository;

import kr.hhplus.be.server.layered.user.model.User;
import kr.hhplus.be.server.layered.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);
}
