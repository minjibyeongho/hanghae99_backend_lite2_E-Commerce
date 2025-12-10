package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.domain.wallet.model.Wallet;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {

    private final WalletJpaRepository walletRepository;
    private final WalletHistoryJpaRepository walletHistoryRepository;

    // 잔액 충전
    @Transactional
    public WalletChargeResponse charge(Long userId, Integer amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다."));

        Integer beforeBalance = wallet.getBalance();
        wallet.addAmount(amount);

        // 이력 저장
        WalletHistory history = WalletHistory.createChargeHistory(wallet, amount, beforeBalance);
        walletHistoryRepository.save(history);
        return new WalletChargeResponse(wallet.getWalletId(), wallet.getBalance());
    }

    // 잔액 조회
    public WalletBalanceResponse getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다"));

        return new WalletBalanceResponse(wallet.getWalletId(), wallet.getBalance());
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

    // DTO
    public record WalletChargeResponse(Long walletId, Integer balance) {}
    public record WalletBalanceResponse(Long walletId, Integer balance) {}
}
