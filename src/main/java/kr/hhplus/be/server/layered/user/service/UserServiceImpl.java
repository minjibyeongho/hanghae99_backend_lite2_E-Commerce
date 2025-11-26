package kr.hhplus.be.server.layered.user.service;

import kr.hhplus.be.server.layered.wallet.model.Wallet;
import kr.hhplus.be.server.layered.wallet.repository.WalletHistoryJpaRepository;
import kr.hhplus.be.server.layered.wallet.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final WalletJpaRepository walletJpaRepository;
    private final WalletHistoryJpaRepository walletHistoryJpaRepository;

    @Override
    public Integer getBalanceByUserId(Long userId) {
        Wallet wallet = walletJpaRepository.findByUserId(userId);
        // 예외처리: 지갑이 없을 경우 에러
        if(wallet == null)
            throw new RuntimeException("사용자 지갑이 존재하지 않습니다.");

        return wallet.getBalance();
    }

    @Override
    public void chargeWalletByUserId(Long userId, Integer amount) {

        // 1. 사용자 지갑 조회
        Wallet wallet = walletJpaRepository.findByUserId(userId);

        // 예외처리: 지갑이 없을 경우 에러
        if(wallet == null)
            throw new RuntimeException("사용자 지갑이 존재하지 않습니다.");

        // 2. 사용자 잔액 지갑 충전
        wallet.addAmount(amount);
        walletJpaRepository.save(wallet);

        // 3. 사용자 지갑 이력 입력

    }

    @Override
    public void substractWalletByUserId(Long userId, Integer amount) {
        // 1. 사용자 지갑 조회
        Wallet wallet = walletJpaRepository.findByUserId(userId);

        // 예외처리: 지갑이 없을 경우 에러
        if(wallet == null)
            throw new RuntimeException("사용자 지갑이 존재하지 않습니다.");

        // 2. 사용자 잔액 지갑 차감
        wallet.substractAmount(amount);
        walletJpaRepository.save(wallet);

        // 3. 사용자 지갑 이력 입력
    }
}
