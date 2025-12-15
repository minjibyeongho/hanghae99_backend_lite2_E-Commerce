package kr.hhplus.be.server.domain.wallet.service;

import kr.hhplus.be.server.common.status.WalletHistoryStatus;
import kr.hhplus.be.server.domain.wallet.model.Wallet;
import kr.hhplus.be.server.domain.wallet.model.WalletHistory;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.infrastructure.wallet.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Transactional
    public WalletHistory processPaymentWithIdempotency(
            Long userId,
            Integer amount,
            String idempotencyKey
    ) {
        // 1. WalletHistory에서 멱등성 체크
        Optional<WalletHistory> existing =
                walletHistoryRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            System.out.println(String.format("중복 결제 요청 감지: idempotencyKey={}", idempotencyKey));
            return existing.get();  // 이미 처리된 결과 반환
        }

        // 2. 최초 요청 - 정상 처리
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        Integer beforeBalance = wallet.getBalance();

        // 3. 조건부 업데이트 (원자적 연산)
        int updated = walletRepository.deductBalanceIfSufficient(userId, amount);

        // 4. 업데이트 실패 처리
        if (updated == 0) {
            // 잔액 부족
            wallet = walletRepository.findByUserId(userId).orElseThrow();

            if (wallet.getBalance() < amount) {
                throw new IllegalStateException(
                        String.format("잔액이 부족합니다. 현재 잔액: %d원, 결제 금액: %d원",
                                wallet.getBalance(), amount)
                );
            } else {
                // 동시성 충돌 또는 기타 원인
                throw new IllegalStateException(
                        "결제 처리 중 오류가 발생했습니다. 다시 시도해주세요."
                );
            }
        }

        // 5. 이력 기록 시 멱등성 키 저장
        WalletHistory history = WalletHistory.builder()
                .walletId(wallet.getWalletId())
                .amount(-amount)
                .beforeBalance(beforeBalance)
                .afterBalance(beforeBalance - amount)
                .status(WalletHistoryStatus.PAYMENT)
                .memo("주문 결제")
                .idempotencyKey(idempotencyKey)  // 멱등성 키 저장
                .build();

        return walletHistoryRepository.save(history);
    }

    // DTO
    public record WalletChargeResponse(Long walletId, Integer balance) {}
    public record WalletBalanceResponse(Long walletId, Integer balance) {}
}
