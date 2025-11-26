package kr.hhplus.be.server.layered.wallet.service;

public class WalletServiceImpl implements WalletService {

    @Override
    public Integer getBalance(Long wallet_id) {
        return 0;
    }

    @Override
    public void addBalance(Long wallet_id, Integer amount) {
        // 1. 지갑 잔액 조회(지갑 유무는 유저에서 지갑 목록 선택시 이미 적용)

        // 2. 해당 지갑 충전

        // 3. 히스토리에 기록

        // 낙관적 락 사용(soft lock)
    }

    @Override
    public Integer subtractBalance(Long wallet_id, Integer amount) {
        // 1. 지갑 잔액 조회(지갑 유무는 유저에서 지갑 목록 선택시 이미 적용)

        // 2. 유효성 검사( 차감 시 잔액 < 차감액: Exception )

        return 0;
    }
}
