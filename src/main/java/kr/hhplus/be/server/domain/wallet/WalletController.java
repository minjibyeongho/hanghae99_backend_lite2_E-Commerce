package kr.hhplus.be.server.domain.wallet;

import kr.hhplus.be.server.domain.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/charge")
    public ResponseEntity<WalletService.WalletChargeResponse> charge(@RequestBody ChargeRequest request) {
        WalletService.WalletChargeResponse response = walletService.charge(request.userId(), request.amount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<WalletService.WalletBalanceResponse> getBalance(@PathVariable Long userId) {
        WalletService.WalletBalanceResponse response = walletService.getBalance(userId);
        return ResponseEntity.ok(response);
    }

    // Request DTO
    public record ChargeRequest(Long userId, Integer amount) {}
}
