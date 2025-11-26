package kr.hhplus.be.server.layered.user.service;

import kr.hhplus.be.server.layered.wallet.model.Wallet;

import java.util.List;


public interface UserService {

    // 과제 구현 API: 잔액 조회/충전 API 구현
    // 사용자는 지갑을 여러개 만들 수 있고, 그 여러개 중 1개를 선택하여 조회 및 충전한다고 가정

    // 사용자 지갑 잔액 조회
    Integer getBalanceByUserId(Long userId);

    // 사용자 지갑 잔액 충전
    void chargeWalletByUserId(Long userId, Integer amount);

    // 사용자 지갑 잔액 차감
    void substractWalletByUserId(Long userId, Integer amount);

    // 사용자 지갑 사용내역 조회( history조회 )
    // TBD




}
