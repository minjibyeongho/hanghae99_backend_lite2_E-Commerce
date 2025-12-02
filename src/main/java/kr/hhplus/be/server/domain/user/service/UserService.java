package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.domain.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.domain.wallet.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final WalletJpaRepository walletJpaRepository;
    private final WalletHistoryJpaRepository walletHistoryJpaRepository;

}
