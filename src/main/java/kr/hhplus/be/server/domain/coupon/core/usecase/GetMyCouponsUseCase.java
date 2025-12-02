package kr.hhplus.be.server.domain.coupon.core.usecase;

import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.domain.coupon.core.port.in.GetMyCouponsQuery;
import kr.hhplus.be.server.domain.coupon.core.port.out.UserCouponPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyCouponsUseCase {
    private final UserCouponPort userCouponPort;

    public List<UserCoupon> execute(GetMyCouponsQuery query) {
        return userCouponPort.findByUserId(query.userId());
    }
}
