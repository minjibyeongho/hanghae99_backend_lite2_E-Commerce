package kr.hhplus.be.server.layered.wallet.repository;

import kr.hhplus.be.server.layered.wallet.model.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletHistoryJpaRepository extends JpaRepository<WalletHistory, Long> {
}
