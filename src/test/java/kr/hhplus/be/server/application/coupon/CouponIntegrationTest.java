package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.TestContainersConfiguration;
import kr.hhplus.be.server.common.status.CouponStatus;
import kr.hhplus.be.server.domain.coupon.core.domain.Coupon;
import kr.hhplus.be.server.domain.coupon.core.domain.UserCoupon;
import kr.hhplus.be.server.domain.coupon.core.facade.CouponIssueFacade;
import kr.hhplus.be.server.domain.coupon.core.port.in.IssueCouponCommand;
import kr.hhplus.be.server.domain.coupon.core.port.out.CouponPort;
import kr.hhplus.be.server.domain.coupon.core.port.out.UserCouponPort;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // ✅ 테스트 순서 보장
public class CouponIntegrationTest {

    @Autowired
    private CouponIssueFacade couponIssueFacade;

    @Autowired
    private CouponPort couponPort;

    @Autowired
    private UserCouponPort userCouponPort;

    private Long testCouponId;

    // ============================================
    // Setup & Teardown
    // ============================================

    @BeforeEach
    void setUp() {
        log.info("===========================================");
        log.info("테스트 데이터 초기화 시작");
        log.info("===========================================");

        // 기존 데이터 삭제
        userCouponPort.deleteAll();
        couponPort.deleteAll();

        // 테스트용 쿠폰 생성
        Coupon coupon = Coupon.builder()
                .couponName("선착순 500명 쿠폰")
                .totalQuantity(500)
                .issuedQuantity(0)
                .version(0L)
                .discountAmount(1000)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .startAt(LocalDateTime.now().minusHours(1))  // 이미 시작됨
                .expiredAt(LocalDateTime.now().plusDays(7))  // 7일 후 만료
                .updatedAt(LocalDateTime.now())
                .build();

        Coupon saved = couponPort.save(coupon);
        testCouponId = saved.getCouponId();

        log.info("테스트 쿠폰 생성 완료: couponId={}, totalQuantity={}", testCouponId, 500);
        log.info("===========================================");
    }

    @AfterEach
    void tearDown() {
        log.info("===========================================");
        log.info("테스트 데이터 정리");
        log.info("===========================================");

        userCouponPort.deleteAll();
        couponPort.deleteAll();
    }

    // ============================================
    // Test Case 1: 단일 발급 성공
    // ============================================

    @Test
    @DisplayName("1명이 쿠폰 발급 성공")
    void issueCoupon_singleUser_success() {
        // Given
        Long userId = 1L;
        IssueCouponCommand command = new IssueCouponCommand(userId, testCouponId);

        log.info("===========================================");
        log.info("Test 1: 단일 발급 테스트");
        log.info("===========================================");

        // When
        UserCoupon result = couponIssueFacade.issueCouponWithRetry(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCouponId()).isEqualTo(testCouponId);
        assertThat(result.getStatus()).isEqualTo(CouponStatus.PREPARING);

        // DB 확인
        Coupon coupon = couponPort.findById(testCouponId).orElseThrow();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        assertThat(coupon.getVersion()).isEqualTo(1L);

        log.info("✅ 발급 성공: userCouponId={}, issuedQuantity={}, version={}",
                result.getUserCouponId(), coupon.getIssuedQuantity(), coupon.getVersion());
        log.info("===========================================");
    }

    // ============================================
    // Test Case 2: 중복 발급 방지
    // ============================================

    @Test
    @DisplayName("같은 사용자가 2번 발급 시도 → 2번째 실패")
    void issueCoupon_duplicateUser_fail() {
        // Given
        Long userId = 1L;
        IssueCouponCommand command = new IssueCouponCommand(userId, testCouponId);

        log.info("===========================================");
        log.info("Test 2: 중복 발급 방지 테스트");
        log.info("===========================================");

        // When - 첫 번째 발급 성공
        UserCoupon first = couponIssueFacade.issueCouponWithRetry(command);
        log.info("1차 발급 성공: userCouponId={}", first.getUserCouponId());

        // Then - 두 번째 발급 실패
        assertThatThrownBy(() -> couponIssueFacade.issueCouponWithRetry(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 발급받은 쿠폰입니다");

        log.info("✅ 2차 발급 차단됨");

        // DB 확인
        Coupon coupon = couponPort.findById(testCouponId).orElseThrow();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(1);  // 1개만 발급됨

        log.info("✅ 발급 수량 정상: issuedQuantity={}", coupon.getIssuedQuantity());
        log.info("===========================================");
    }

    // ============================================
    // Test Case 3: 순차 발급 (100명)
    // ============================================

    @Test
    @DisplayName("100명이 순차적으로 발급 → 모두 성공")
    void issueCoupon_100Users_sequential_success() {
        // Given
        int userCount = 100;

        log.info("===========================================");
        log.info("Test 3: 순차 발급 테스트 ({}명)", userCount);
        log.info("===========================================");

        // When
        for (long userId = 1; userId <= userCount; userId++) {
            IssueCouponCommand command = new IssueCouponCommand(userId, testCouponId);
            couponIssueFacade.issueCouponWithRetry(command);

            if (userId % 20 == 0) {
                log.info("진행 중: {}명 발급 완료", userId);
            }
        }

        // Then
        Coupon coupon = couponPort.findById(testCouponId).orElseThrow();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(userCount);
        assertThat(coupon.getVersion()).isEqualTo((long) userCount);

        List<UserCoupon> userCoupons = userCouponPort.findByCouponId(testCouponId);
        assertThat(userCoupons).hasSize(userCount);

        log.info("✅ {}명 발급 완료: issuedQuantity={}, version={}",
                userCount, coupon.getIssuedQuantity(), coupon.getVersion());
        log.info("===========================================");
    }

    // ============================================
    // Test Case 4: 🔥 동시성 테스트 (핵심)
    // ============================================

    @Test
    @DisplayName("🔥 1000명이 동시 발급 → 정확히 500명만 성공")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)  // 60초 타임아웃
    void issueCoupon_1000Users_concurrent_only500Success() throws InterruptedException {
        // Given
        int threadCount = 1000;
        int expectedSuccess = 500;

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // ThreadPool 설정 개선
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        ConcurrentHashMap<Long, String> results = new ConcurrentHashMap<>();

        log.info("===========================================");
        log.info("Test 4: 🔥 동시성 테스트");
        log.info("===========================================");
        log.info("총 요청: {}명", threadCount);
        log.info("쿠폰 수량: {}개", expectedSuccess);
        log.info("예상 성공: {}명", expectedSuccess);
        log.info("예상 실패: {}명", threadCount - expectedSuccess);
        log.info("===========================================");

        long startTime = System.currentTimeMillis();

        // When - 1000명이 동시에 발급 시도
        for (long userId = 1; userId <= threadCount; userId++) {
            long finalUserId = userId;
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 준비될 때까지 대기
                    readyLatch.countDown();
                    startLatch.await();

                    // 쿠폰 발급 시도
                    IssueCouponCommand command = new IssueCouponCommand(finalUserId, testCouponId);
                    UserCoupon result = couponIssueFacade.issueCouponWithRetry(command);

                    successCount.incrementAndGet();
                    results.put(finalUserId, "SUCCESS: " + result.getUserCouponId());

                } catch (IllegalStateException e) {
                    failCount.incrementAndGet();
                    results.put(finalUserId, "FAIL: " + e.getMessage());

                } catch (Exception e) {
                    failCount.incrementAndGet();
                    results.put(finalUserId, "ERROR: " + e.getMessage());
                    log.error("예상치 못한 예외: userId={}, error={}", finalUserId, e.getMessage());

                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 모든 스레드가 준비될 때까지 대기
        readyLatch.await();
        log.info("모든 스레드 준비 완료. 동시 발급 시작!");

        // 동시 시작
        startLatch.countDown();

        // 모든 스레드가 완료될 때까지 대기 (최대 30초)
        boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
        assertThat(finished).isTrue();

        executorService.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - 검증
        log.info("===========================================");
        log.info("동시성 테스트 결과");
        log.info("===========================================");
        log.info("총 요청: {}명", threadCount);
        log.info("성공: {}명", successCount.get());
        log.info("실패: {}명", failCount.get());
        log.info("소요 시간: {}ms", duration);
        log.info("===========================================");

        // 1. 정확히 500명만 성공
        assertThat(successCount.get()).isEqualTo(expectedSuccess);
        assertThat(failCount.get()).isEqualTo(threadCount - expectedSuccess);

        // 2. DB 검증
        Coupon coupon = couponPort.findById(testCouponId).orElseThrow();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(expectedSuccess);

        log.info("✅ DB 검증 완료");
        log.info("   - issued_quantity: {}/{}", coupon.getIssuedQuantity(), coupon.getTotalQuantity());
        log.info("   - version: {}", coupon.getVersion());

        // 3. UserCoupon 검증
        List<UserCoupon> userCoupons = userCouponPort.findByCouponId(testCouponId);
        assertThat(userCoupons).hasSize(expectedSuccess);

        log.info("✅ UserCoupon 검증 완료");
        log.info("   - 발급된 쿠폰 수: {}", userCoupons.size());

        // 4. 중복 발급 확인
        long uniqueUserCount = userCoupons.stream()
                .map(UserCoupon::getUserId)
                .distinct()
                .count();
        assertThat(uniqueUserCount).isEqualTo(expectedSuccess);

        log.info("✅ 중복 발급 없음: 고유 사용자 수 = {}", uniqueUserCount);

        // 5. 실패 사유 분석
        long soldOutCount = results.values().stream()
                .filter(msg -> msg.contains("소진"))
                .count();

        log.info("===========================================");
        log.info("실패 사유 분석");
        log.info("===========================================");
        log.info("수량 소진: {}명", soldOutCount);
        log.info("기타: {}명", failCount.get() - soldOutCount);
        log.info("===========================================");

        log.info("🎉 동시성 테스트 성공!");
        log.info("===========================================");
    }

    // ============================================
    // Test Case 5: 수량 초과 발급 방지
    // ============================================

    @Test
    @DisplayName("500개 발급 후 501번째 발급 시도 → 실패")
    void issueCoupon_exceedQuantity_fail() {
        // Given
        log.info("===========================================");
        log.info("Test 5: 수량 초과 방지 테스트");
        log.info("===========================================");

        // 500명 발급
        for (long userId = 1; userId <= 500; userId++) {
            IssueCouponCommand command = new IssueCouponCommand(userId, testCouponId);
            couponIssueFacade.issueCouponWithRetry(command);
        }

        log.info("500명 발급 완료");

        // When & Then - 501번째 실패
        IssueCouponCommand command = new IssueCouponCommand(501L, testCouponId);

        assertThatThrownBy(() -> couponIssueFacade.issueCouponWithRetry(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("소진");

        log.info("✅ 501번째 발급 차단됨");

        // DB 확인
        Coupon coupon = couponPort.findById(testCouponId).orElseThrow();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(500);

        log.info("✅ 발급 수량 정상: {}/500", coupon.getIssuedQuantity());
        log.info("===========================================");
    }

    // ============================================
    // Test Case 6: 만료된 쿠폰 발급 방지
    // ============================================

    @Test
    @DisplayName("만료된 쿠폰 발급 시도 → 실패")
    void issueCoupon_expiredCoupon_fail() {
        // Given
        log.info("===========================================");
        log.info("Test 6: 만료 쿠폰 발급 방지 테스트");
        log.info("===========================================");

        // 만료된 쿠폰 생성
        Coupon expiredCoupon = Coupon.builder()
                .couponName("만료된 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .version(0L)
                .discountAmount(1000)
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(10))
                .startAt(LocalDateTime.now().minusDays(9))
                .expiredAt(LocalDateTime.now().minusDays(1))  // 이미 만료됨
                .updatedAt(LocalDateTime.now())
                .build();

        Coupon saved = couponPort.save(expiredCoupon);

        log.info("만료된 쿠폰 생성: expiredAt={}", saved.getExpiredAt());

        // When & Then
        IssueCouponCommand command = new IssueCouponCommand(1L, saved.getCouponId());

        assertThatThrownBy(() -> couponIssueFacade.issueCouponWithRetry(command))
                .isInstanceOf(IllegalStateException.class);

        log.info("✅ 만료된 쿠폰 발급 차단됨");
        log.info("===========================================");
    }

    // ============================================
    // Test Case 7: 비활성화된 쿠폰 발급 방지
    // ============================================

    @Test
    @DisplayName("비활성화된 쿠폰 발급 시도 → 실패")
    void issueCoupon_inactiveCoupon_fail() {
        // Given
        log.info("===========================================");
        log.info("Test 7: 비활성화 쿠폰 발급 방지 테스트");
        log.info("===========================================");

        // 비활성화된 쿠폰 생성
        Coupon inactiveCoupon = Coupon.builder()
                .couponName("비활성화 쿠폰")
                .totalQuantity(100)
                .issuedQuantity(0)
                .version(0L)
                .discountAmount(1000)
                .isActive(false)  // 비활성화
                .createdAt(LocalDateTime.now())
                .startAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(7))
                .updatedAt(LocalDateTime.now())
                .build();

        Coupon saved = couponPort.save(inactiveCoupon);

        log.info("비활성화된 쿠폰 생성: isActive={}", saved.getIsActive());

        // When & Then
        IssueCouponCommand command = new IssueCouponCommand(1L, saved.getCouponId());

        assertThatThrownBy(() -> couponIssueFacade.issueCouponWithRetry(command))
                .isInstanceOf(IllegalStateException.class);

        log.info("✅ 비활성화된 쿠폰 발급 차단됨");
        log.info("===========================================");
    }
}
