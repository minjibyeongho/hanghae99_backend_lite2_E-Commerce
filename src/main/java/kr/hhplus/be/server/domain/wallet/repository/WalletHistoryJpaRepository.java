package kr.hhplus.be.server.domain.wallet.repository;

import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletHistoryJpaRepository extends JpaRepository<WalletHistory, Long> {
}
