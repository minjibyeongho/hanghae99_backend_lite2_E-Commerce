package kr.hhplus.be.server.layered.user.service;

import kr.hhplus.be.server.common.status.WalletHistoryStatus;
import kr.hhplus.be.server.layered.user.model.User;
import kr.hhplus.be.server.layered.wallet.model.Wallet;
import kr.hhplus.be.server.layered.wallet.model.WalletHistory;
import kr.hhplus.be.server.layered.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.layered.wallet.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl {

    private final WalletJpaRepository walletJpaRepository;
    private final WalletHistoryJpaRepository walletHistoryJpaRepository;

}
