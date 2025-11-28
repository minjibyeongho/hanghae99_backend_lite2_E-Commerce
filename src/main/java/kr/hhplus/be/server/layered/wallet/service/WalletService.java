package kr.hhplus.be.server.layered.wallet.service;

import kr.hhplus.be.server.layered.wallet.model.Wallet;
import kr.hhplus.be.server.layered.wallet.model.WalletHistory;
import kr.hhplus.be.server.layered.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.layered.wallet.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl {

    private final WalletJpaRepository walletRepository;
    private final WalletHistoryJpaRepository walletHistoryRepository;

    @Transactional
    public WalletHistory charge(Long userId, Integer amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다."));

        Integer beforeBalance = wallet.getBalance();
        wallet.addAmount(amount);

        // 이력 저장
        WalletHistory history = WalletHistory.createChargeHistory(wallet, amount, beforeBalance);
        return walletHistoryRepository.save(history);
    }

    // 잔액 조회
    public Integer getBalance(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getBalance)
                .orElse(0);
    }

    @Transactional
    public WalletHistory processPayment(Long userId, Integer amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다."));

        Integer beforeBalance = wallet.getBalance();
        wallet.substractAmount(amount);

        WalletHistory history = WalletHistory.createPaymentHistory(wallet, amount, beforeBalance);
        return walletHistoryRepository.save(history);
    }
}
