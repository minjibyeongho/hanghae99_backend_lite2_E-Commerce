package kr.hhplus.be.server.layered.wallet.repository;

import kr.hhplus.be.server.layered.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {

    // 지갑 조회
    Wallet findByUserId(Long userId);

}
