package kr.hhplus.be.server.layered.coupon;

import kr.hhplus.be.server.layered.coupon.model.UserCoupon;
import kr.hhplus.be.server.layered.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {


    private final CouponService couponService;

    /**
     * 선착순 쿠폰 발급 API
     */
    @PostMapping("/issue")
    public ResponseEntity<CouponService.UserCouponResponse> issueCoupon(@RequestBody IssueRequest request) {
        CouponService.UserCouponResponse response = couponService.issueCoupon(request.userId(), request.couponId());
        return ResponseEntity.ok(response);
    }

    /**
     * 보유 쿠폰 목록 조회 API
     */
    @GetMapping("/my/{userId}")
    public ResponseEntity<List<CouponService.UserCouponResponse>> getMyCoupons(@PathVariable Long userId) {
        List<CouponService.UserCouponResponse> response = couponService.getMyCoupons(userId);
        return ResponseEntity.ok(response);
    }

    // Request DTO
    public record IssueRequest(Long userId, Long couponId) {}
}
