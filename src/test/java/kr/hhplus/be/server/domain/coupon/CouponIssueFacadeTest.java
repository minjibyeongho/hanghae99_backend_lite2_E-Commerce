package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.domain.coupon.core.facade.CouponIssueFacade;
import kr.hhplus.be.server.domain.coupon.core.port.in.IssueCouponCommand;
import kr.hhplus.be.server.domain.coupon.core.usecase.IssueCouponUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouponIssueFacadeTest {
    @Mock
    private IssueCouponUseCase issueCouponUseCase;

    @InjectMocks
    private CouponIssueFacade couponIssueFacade;

    @Test
    @DisplayName("첫 시도에 성공")
    void issueCouponWithRetry_firstTry_success() {
        // Given
        IssueCouponCommand command = new IssueCouponCommand(1L, 1L);
        UserCoupon expected = UserCoupon.builder()
                .userCouponId(1L)
                .userId(1L)
                .couponId(1L)
                .build();

        when(issueCouponUseCase.execute(any())).thenReturn(expected);

        // When
        UserCoupon result = couponIssueFacade.issueCouponWithRetry(command);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(issueCouponUseCase, times(1)).execute(command);
    }

    @Test
    @DisplayName("재시도 후 성공")
    void issueCouponWithRetry_retryAndSuccess() {
        // Given
        IssueCouponCommand command = new IssueCouponCommand(1L, 1L);
        UserCoupon expected = UserCoupon.builder()
                .userCouponId(1L)
                .build();

        // 처음 2번은 충돌, 3번째에 성공
        when(issueCouponUseCase.execute(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException("충돌", null))
                .thenThrow(new ObjectOptimisticLockingFailureException("충돌", null))
                .thenReturn(expected);

        // When
        UserCoupon result = couponIssueFacade.issueCouponWithRetry(command);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(issueCouponUseCase, times(3)).execute(command);
    }

    @Test
    @DisplayName("비즈니스 예외는 재시도 안 함")
    void issueCouponWithRetry_businessException_noRetry() {
        // Given
        IssueCouponCommand command = new IssueCouponCommand(1L, 1L);

        when(issueCouponUseCase.execute(any()))
                .thenThrow(new IllegalStateException("쿠폰 소진"));

        // When & Then
        assertThatThrownBy(() -> couponIssueFacade.issueCouponWithRetry(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("쿠폰 소진");

        verify(issueCouponUseCase, times(1)).execute(command);  // 1번만 호출
    }
}
