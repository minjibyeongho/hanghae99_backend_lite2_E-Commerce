package kr.hhplus.be.server.layered.wallet.service;

public interface WalletService {

    // 유저 - 지갑은 1:1 매칭으로 가정

    // 잔액 조회
    Integer getBalance(Long wallet_id);

    // 잔액 충전
    void addBalance(Long wallet_id, Integer amount);

    // 잔액 차감(차감 후 변경된 잔액 리턴)
    Integer subtractBalance(Long wallet_id, Integer amount);

}
