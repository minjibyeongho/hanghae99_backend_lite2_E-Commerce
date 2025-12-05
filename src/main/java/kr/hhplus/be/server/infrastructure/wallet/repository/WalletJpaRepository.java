package kr.hhplus.be.server.infrastructure.wallet.repository;

import kr.hhplus.be.server.domain.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);
}
