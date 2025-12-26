## 샘플 DB 넣기
### 샘플 쿼리
- 1. 쿠폰 데이터 확인
SELECT * FROM coupon WHERE coupon_id = 1;


- 쿠폰이 없다면 생성
INSERT INTO coupon (
    coupon_id,
    coupon_name,
    total_quantity,
    issued_quantity,
    discount_amount,
    discount_rate,
    is_active,
    start_at,
    expired_at,
    created_at,
    version
) VALUES (
    1,
    '신규가입 10% 할인 쿠폰',
    1000,              -- 총 1000개 발급 가능
    0,                 -- 아직 발급 안됨
    NULL,              -- 고정 금액 할인 (NULL이면 비율 사용)
    10,                -- 10% 할인
    TRUE,              -- 활성화
    NOW(),             -- 지금부터 시작
    DATE_ADD(NOW(), INTERVAL 30 DAY),  -- 30일 후 만료
    NOW(),
    0                  -- 버전 (Optimistic Lock용)
);


- 쿠폰 데이터 확인
SELECT
    coupon_id,
    coupon_name,
    total_quantity,
    issued_quantity,
    (total_quantity - issued_quantity) as remaining,
    is_active,
    expired_at
FROM 
    coupon
WHERE 
    coupon_id = 1;


- 사용자가 부족하면 대량 생성 (1~1000번)
INSERT INTO user (user_name, user_email, user_password, user_phone, created_at)
SELECT
CONCAT('test_user_', n) as user_name,
CONCAT('test', n, '@example.com') as user_email,
'password123' as user_password,
CONCAT('010-', LPAD(n, 4, '0'), '-', LPAD(n, 4, '0')) as user_phone,
NOW() as created_at
FROM (
SELECT @row := @row + 1 as n
FROM information_schema.columns c1,
information_schema.columns c2,
(SELECT @row := 0) r
LIMIT 1000
) numbers
WHERE NOT EXISTS (
SELECT 1 FROM user WHERE user_id = n
);


- 사용자 수 확인
SELECT COUNT(*) as user_count FROM user;

- 특정 범위 사용자 확인
SELECT user_id, user_name, user_email
FROM user
WHERE user_id BETWEEN 1 AND 1000
LIMIT 10;

- 발급된 쿠폰 내역 확인
SELECT COUNT(*) as issued_count
FROM user_coupon
WHERE coupon_id = 1;

- 테스트 데이터 초기화(주의: 실제 운영 DB에서는 절대 실행하지 마세요!)
DELETE FROM user_coupon WHERE coupon_id = 1;

- 쿠폰의 issued_quantity 초기화
UPDATE coupon
SET issued_quantity = 0, version = 0
WHERE coupon_id = 1;

- 초기화 확인
SELECT
coupon_id,
issued_quantity,
total_quantity,
(total_quantity - issued_quantity) as remaining
FROM coupon
WHERE coupon_id = 1;


- 종합 체크
SELECT
    '서버 상태' as check_item,
    '브라우저나 curl로 API 호출 테스트' as status
UNION ALL
SELECT
    '쿠폰 데이터',
    CASE
        WHEN EXISTS(SELECT 1 FROM coupon WHERE coupon_id = 1 AND is_active = TRUE)
        THEN CONCAT('✓ 준비됨 (남은 수량: ',
        (SELECT total_quantity - issued_quantity FROM coupon WHERE coupon_id = 1),
        '개)')
        ELSE '✗ 쿠폰 데이터 생성 필요'
    END
UNION ALL
    SELECT
    '사용자 데이터',
    CASE
        WHEN (SELECT COUNT(*) FROM user WHERE user_id BETWEEN 1 AND 100) >= 10
        THEN CONCAT('✓ 준비됨 (', (SELECT COUNT(*) FROM user), '명)')
        ELSE '✗ 사용자 데이터 생성 필요'
END;

======================================================================
### k6 테스트 결과
- 1000명 무작위 실행 -> 고유 user당 실행하는 테스트로 변경
- 실제로는 동일 유저가 동일 쿠폰을 조회하는 경우도 나눠서 테스트 해야하나 부하테스트 선 진행 후 진행 예정
- 에러율 0%까지는 못했으나 근사치까지는 나오는 것 확인(제일 마지막 테스트 결과)


         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
/  \/    \    | |/ /  /   ‾‾\
/          \   |   (  |  (‾)  |
/ __________ \  |_|\_\  \_____/

     execution: local
        script: ./testScript/coupon-load-test.js
        output: -

     scenarios: (100.00%) 1 scenario, 5 max VUs, 1m0s max duration (incl. graceful stop):
              * default: 5 looping VUs for 30s (gracefulStop: 30s)



█ THRESHOLDS

    errors
    ✓ 'rate<0.1' rate=7.43%

    http_req_duration
    ✓ 'p(95)<500' p(95)=31.5ms


█ TOTAL RESULTS

    checks_total.......: 444    14.325076/s
    checks_succeeded...: 95.27% 423 out of 444
    checks_failed......: 4.72%  21 out of 444

    ✗ status is 200
      ↳  93% — ✓ 138 / ✗ 10
    ✗ response has userCouponId
      ↳  93% — ✓ 138 / ✗ 10
    ✗ response time < 500ms
      ↳  99% — ✓ 147 / ✗ 1

    CUSTOM
    errors.........................: 7.43% 11 out of 148

    HTTP
    http_req_duration..............: avg=34.8ms  min=5.54ms med=20.97ms max=925.05ms p(90)=27.68ms p(95)=31.5ms
      { expected_response:true }...: avg=36.01ms min=5.54ms med=21.08ms max=925.05ms p(90)=27.68ms p(95)=46.63ms
    http_req_failed................: 6.75% 10 out of 148
    http_reqs......................: 148   4.775025/s

    EXECUTION
    iteration_duration.............: avg=1.03s   min=1s     med=1.02s   max=1.92s    p(90)=1.02s   p(95)=1.03s
    iterations.....................: 148   4.775025/s
    vus............................: 5     min=5         max=5
    vus_max........................: 5     min=5         max=5

    NETWORK
    data_received..................: 27 kB 878 B/s
    data_sent......................: 25 kB 797 B/s




running (0m31.0s), 0/5 VUs, 148 complete and 0 interrupted iterations




         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
/  \/    \    | |/ /  /   ‾‾\
/          \   |   (  |  (‾)  |
/ __________ \  |_|\_\  \_____/

     execution: local
        script: ./testScript/coupon-load-test.js
        output: -

     scenarios: (100.00%) 1 scenario, 5 max VUs, 1m0s max duration (incl. graceful stop):
              * default: 5 looping VUs for 30s (gracefulStop: 30s)



█ THRESHOLDS

    errors
    ✓ 'rate<0.1' rate=9.45%

    http_req_duration
    ✓ 'p(95)<500' p(95)=25.29ms


█ TOTAL RESULTS

    checks_total.......: 444    14.322947/s
    checks_succeeded...: 94.14% 418 out of 444
    checks_failed......: 5.85%  26 out of 444

    ✗ status is 200
      ↳  91% — ✓ 136 / ✗ 12
    ✗ response has userCouponId
      ↳  91% — ✓ 136 / ✗ 12
    ✗ response time < 500ms
      ↳  98% — ✓ 146 / ✗ 2

    CUSTOM
    errors.........................: 9.45% 14 out of 148

    HTTP
    http_req_duration..............: avg=32.74ms min=5.45ms med=19.2ms  max=788.4ms p(90)=23.36ms p(95)=25.29ms
      { expected_response:true }...: avg=34.52ms min=5.65ms med=19.76ms max=788.4ms p(90)=23.42ms p(95)=25.91ms
    http_req_failed................: 8.10% 12 out of 148
    http_reqs......................: 148   4.774316/s

    EXECUTION
    iteration_duration.............: avg=1.03s   min=1s     med=1.02s   max=1.78s   p(90)=1.02s   p(95)=1.02s
    iterations.....................: 148   4.774316/s
    vus............................: 5     min=5         max=5
    vus_max........................: 5     min=5         max=5

    NETWORK
    data_received..................: 28 kB 887 B/s
    data_sent......................: 25 kB 797 B/s

running (0m31.0s), 0/5 VUs, 148 complete and 0 interrupted iterations



- 스크립트에 에러 출력 추가(에러율 0% 만들기!)

         /\      Grafana   /‾‾/
  /\  /  \     |\  __   /  /
  /  \/    \    | |/ /  /   ‾‾\
  /          \   |   (  |  (‾)  |
  / __________ \  |_|\_\  \_____/

  execution: local
  script: ./testScript/coupon-load-test.js
  output: -

  scenarios: (100.00%) 1 scenario, 5 max VUs, 1m0s max duration (incl. graceful stop):
  * default: 5 looping VUs for 30s (gracefulStop: 30s)

INFO[0003] ❌ 실패! Status: 500                             source=console
INFO[0003] Response Body: {"timestamp":"2025-12-26T15:11:58.456+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0003] User ID: 369                                  source=console
INFO[0005] ❌ 실패! Status: 500                             source=console
INFO[0005] Response Body: {"timestamp":"2025-12-26T15:12:00.158+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0005] User ID: 318                                  source=console
INFO[0014] ❌ 실패! Status: 500                             source=console
INFO[0014] Response Body: {"timestamp":"2025-12-26T15:12:09.008+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0014] User ID: 768                                  source=console
INFO[0015] ❌ 실패! Status: 500                             source=console
INFO[0015] Response Body: {"timestamp":"2025-12-26T15:12:10.022+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0015] User ID: 749                                  source=console
INFO[0021] ❌ 실패! Status: 500                             source=console
INFO[0021] Response Body: {"timestamp":"2025-12-26T15:12:16.723+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0021] User ID: 559                                  source=console
INFO[0021] ❌ 실패! Status: 500                             source=console
INFO[0021] Response Body: {"timestamp":"2025-12-26T15:12:16.783+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0021] User ID: 156                                  source=console
INFO[0024] ❌ 실패! Status: 500                             source=console
INFO[0024] Response Body: {"timestamp":"2025-12-26T15:12:19.436+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0024] User ID: 591                                  source=console
INFO[0026] ❌ 실패! Status: 500                             source=console
INFO[0026] Response Body: {"timestamp":"2025-12-26T15:12:21.477+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0026] User ID: 579                                  source=console
INFO[0026] ❌ 실패! Status: 500                             source=console
INFO[0026] Response Body: {"timestamp":"2025-12-26T15:12:21.851+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0026] User ID: 749                                  source=console
INFO[0028] ❌ 실패! Status: 500                             source=console
INFO[0028] Response Body: {"timestamp":"2025-12-26T15:12:23.516+00:00","status":500,"error":"Internal Server Error","path":"/api/coupons/issue"}  source=console
INFO[0028] User ID: 244                                  source=console


█ THRESHOLDS

    errors
    ✓ 'rate<0.1' rate=7.38%

    http_req_duration
    ✓ 'p(95)<500' p(95)=25.36ms


█ TOTAL RESULTS

    checks_total.......: 447    14.43954/s
    checks_succeeded...: 95.30% 426 out of 447
    checks_failed......: 4.69%  21 out of 447

    ✗ status is 200
      ↳  93% — ✓ 139 / ✗ 10
    ✗ response has userCouponId
      ↳  93% — ✓ 139 / ✗ 10
    ✗ response time < 500ms
      ↳  99% — ✓ 148 / ✗ 1

    CUSTOM
    errors.........................: 7.38% 11 out of 149

    HTTP
    http_req_duration..............: avg=28.67ms min=4.26ms med=18.19ms max=779.06ms p(90)=23.57ms p(95)=25.36ms
      { expected_response:true }...: avg=29.9ms  min=6.9ms  med=18.61ms max=779.06ms p(90)=23.59ms p(95)=25.61ms
    http_req_failed................: 6.71% 10 out of 149
    http_reqs......................: 149   4.81318/s

    EXECUTION
    iteration_duration.............: avg=1.03s   min=1s     med=1.01s   max=1.78s    p(90)=1.02s   p(95)=1.02s
    iterations.....................: 149   4.81318/s
    vus............................: 5     min=5         max=5
    vus_max........................: 5     min=5         max=5

    NETWORK
    data_received..................: 28 kB 888 B/s
    data_sent......................: 25 kB 803 B/s




running (0m31.0s), 0/5 VUs, 149 complete and 0 interrupted iterations

비즈니스 예외 발생 (재시도 0회 후): userId=749, couponId=1, error=이미 발급받은 쿠폰: userId=749, couponId=1
2025-12-27T00:12:21.851+09:00 ERROR 18202 --- [hhplus] [nio-8080-exec-6] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.IllegalStateException: 이미 발급받은 쿠폰: userId=749, couponId=1] with root cause

java.lang.IllegalStateException: 이미 발급받은 쿠폰: userId=749, couponId=1
at kr.hhplus.be.server.domain.coupon.core.usecase.IssueCouponUseCase.execute(IssueCouponUseCase.java:31) ~[main/:na]
...

- 현재 단계에서는 k6가 제대로 동작하는지를 확인하기 위한 것으로 같은 유저가 같은 쿠폰 번호를 여러번 요청하는 경우 에러 발생하는 경우는 제외( 테스트 재실행 )



         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
/  \/    \    | |/ /  /   ‾‾\
/          \   |   (  |  (‾)  |
/ __________ \  |_|\_\  \_____/

     execution: local
        script: ./testScript/coupon-load-test.js
        output: -

     scenarios: (100.00%) 1 scenario, 5 max VUs, 1m0s max duration (incl. graceful stop):
              * default: 5 looping VUs for 30s (gracefulStop: 30s)



█ THRESHOLDS

    errors
    ✓ 'rate<0.01' rate=0.67%

    http_req_duration
    ✓ 'p(95)<500' p(95)=21.73ms


█ TOTAL RESULTS

    checks_total.......: 447    14.53949/s
    checks_succeeded...: 99.77% 446 out of 447
    checks_failed......: 0.22%  1 out of 447

    ✓ status is 200
    ✓ response has userCouponId
    ✗ response time < 500ms
      ↳  99% — ✓ 148 / ✗ 1

    CUSTOM
    errors.........................: 0.67% 1 out of 149

    HTTP
    http_req_duration..............: avg=24.24ms min=5.71ms med=15.5ms max=764.89ms p(90)=19.03ms p(95)=21.73ms
      { expected_response:true }...: avg=24.24ms min=5.71ms med=15.5ms max=764.89ms p(90)=19.03ms p(95)=21.73ms
    http_req_failed................: 0.00% 0 out of 149
    http_reqs......................: 149   4.846497/s

    EXECUTION
    iteration_duration.............: avg=1.02s   min=1s     med=1.01s  max=1.76s    p(90)=1.02s   p(95)=1.02s
    iterations.....................: 149   4.846497/s
    vus............................: 5     min=5        max=5
    vus_max........................: 5     min=5        max=5

    NETWORK
    data_received..................: 27 kB 868 B/s
    data_sent......................: 25 kB 806 B/s




running (0m30.7s), 0/5 VUs, 149 complete and 0 interrupted iterations