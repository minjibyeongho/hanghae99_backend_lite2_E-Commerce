package kr.hhplus.be.server.domain.coupon.adapter.in.web;

import kr.hhplus.be.server.domain.coupon.adapter.in.web.dto.IssueCouponRequest;
import kr.hhplus.be.server.domain.coupon.adapter.in.web.dto.UserCouponResponse;
import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.domain.coupon.core.facade.CouponIssueFacade;
import kr.hhplus.be.server.domain.coupon.core.port.in.GetMyCouponsQuery;
import kr.hhplus.be.server.domain.coupon.core.port.in.IssueCouponCommand;
import kr.hhplus.be.server.domain.coupon.core.usecase.GetMyCouponsUseCase;
import kr.hhplus.be.server.domain.coupon.core.usecase.IssueCouponUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponIssueFacade couponIssueFacade;
    private final IssueCouponUseCase issueCouponUseCase;
    private final GetMyCouponsUseCase getMyCouponsUseCase;

    /**
     * 선착순 쿠폰 발급 API
     *
     * POST /api/coupons/issue
     *
     * Request Body:
     * {
     *   "userId": 1,
     *   "couponId": 1
     * }
     *
     * Response:
     * {
     *   "userCouponId": 123,
     *   "userId": 1,
     *   "couponId": 1,
     *   "status": "PREPARING",
     *   "issuedAt": "2025-12-14T23:00:00",
     *   "expireAt": "2025-12-21T23:00:00"
     * }
     */
    @PostMapping("/issue")
    public ResponseEntity<UserCouponResponse> issueCoupon(@RequestBody IssueCouponRequest request) {
        IssueCouponCommand command = new IssueCouponCommand(request.userId(), request.couponId());


        // Facade를 통해 재시도 로직 포함된 발급
        UserCoupon userCoupon = couponIssueFacade.issueCouponWithRetry(command);
        // UserCoupon userCoupon = issueCouponUseCase.execute(command);

        return ResponseEntity.ok(UserCouponResponse.from(userCoupon));
    }

    /**
     * 보유 쿠폰 목록 조회 API
     */
    @GetMapping("/my/{userId}")
    public ResponseEntity<List<UserCouponResponse>> getMyCoupons(@PathVariable Long userId) {
        GetMyCouponsQuery query = new GetMyCouponsQuery(userId);
        List<UserCoupon> userCoupons = getMyCouponsUseCase.execute(query);

        List<UserCouponResponse> response = userCoupons.stream()
                .map(UserCouponResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}
