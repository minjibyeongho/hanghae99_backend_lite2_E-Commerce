## 쿠폰 TPS 테스트

### 1. 테스트 전 준비(샘플 쿼리)

- 충분한 유저 수 확인 (최소 20,000명 필요)
- TPS 300 × 180초 = 54,000개 요청 예상
SELECT COUNT(*) FROM user;

- 부족하면 대량 생성 (20,000명)
INSERT INTO user (user_id, user_name, user_email, user_password, user_phone, created_at)
SELECT
    n as user_id,
    CONCAT('loadtest_user_', n) as user_name,
    CONCAT('loadtest', n, '@example.com') as user_email,
    'password123' as user_password,
    CONCAT('010-', LPAD(n MOD 10000, 4, '0'), '-', LPAD(n DIV 10000, 4, '0')) as user_phone,
    NOW() as created_at
FROM (
    SELECT (@row := @row + 1) as n
    FROM information_schema.columns c1,
    information_schema.columns c2,
    information_schema.columns c3,
    (SELECT @row := 0) r
    LIMIT 20000
    ) numbers
WHERE NOT EXISTS (SELECT 1 FROM user WHERE user_id = n);

- 충분한 쿠폰 수량 설정 (100,000개)
UPDATE coupon
SET 
  total_quantity = 100000,
  issued_quantity = 0,
  version = 0
WHERE 
  coupon_id = 1;

- 확인
SELECT 
    coupon_id, total_quantity, issued_quantity,
    (total_quantity - issued_quantity) as remaining
FROM 
    coupon 
WHERE 
    coupon_id = 1;

- 테스트 전 초기화
DELETE FROM user_coupon WHERE coupon_id = 1;

UPDATE coupon
SET issued_quantity = 0,
version = 0
WHERE coupon_id = 1;

### 2. 테스트 후 결과 저장
- document/result/coupon-tps 폴더 내 저장

### 3. 테스트 결과 요약(with AI)
#### 3-1. 📊 종합 분석 테이블
| 지표 | TPS 50 | TPS 100 | TPS 300 | 목표 | 달성 여부 |
|------|--------|---------|---------|------|-----------|
| 실제 TPS | 50.00 | 99.99 | 158.33 | - | TPS 300 미달 ⚠️ |
| 총 요청 수 | 9,001 | 18,000 | 56,999 | - | - |
| 실행 시간 | 3분 | 3분 | 6분 | - | - |
| 평균 Latency | 10.16ms | 9.43ms | 32.48ms | - | 매우 우수 ✅ |
| 중앙값 Latency | 9.87ms | 6.01ms | 3.23ms | - | 매우 우수 ✅ |
| p90 Latency | 13.38ms | 8.86ms | 9.86ms | - | 매우 우수 ✅ |
| p95 Latency | 14.47ms | 10.28ms | 105.83ms | 500ms | 모두 달성 ✅ |
| p99 Latency | 17.56ms | 111.79ms | 717.93ms | 1000ms | TPS 300만 약간 높음 ⚠️ |
| 최대 Latency | 738.8ms | 3.56s | 6.58s | - | 일부 느린 요청 있음 ⚠️ |
| HTTP 실패율 | 0.00% | 0.00% | 0.01% | 1% | 모두 달성 ✅ |
| 체크 실패율 | 0.00% | 0.04% | 0.26% | - | 양호 ✅ |
| 에러율 | 0.01% | 0.12% | 0.69% | 1% | 모두 달성 ✅ |
| 최대 VU 사용 | 10 | 20 | 50 | - | - |
| 실제 최대 VU | 2 | 3 | 30 | - | 효율적 ✅ |

#### 3-2. 전반적 사항
✅ 모든 TPS에서 p95 목표(500ms) 달성
✅ 실패율 1% 미만 달성
✅ 대부분의 응답이 매우 빠름 (중앙값 3~10ms)
⚠️  TPS 300에서 목표치 미달 (158 TPS만 달성)

#### 3-3. Latency 분석
- 극소수의 요청이 매우 느림 (p99 = 717ms, max = 6.58s)

#### 3-4. 실패율 & 안정성 분석
- HTTP 실패율: 0% ~ 0.01% (거의 없음)
- 에러율: 0.01% ~ 0.69% (목표 1% 이하)
- 체크 성공률: 99.73% ~ 99.99%
- 확인해야할 사항(에러 로그 6개, TPS 300 테스트 시 발생)
INFO[0222] ❌ userId=49330, status=500
INFO[0227] ❌ userId=52247, status=500
INFO[0278] ❌ userId=77440, status=500
INFO[0287] ❌ userId=85505, status=500
INFO[0311] ❌ userId=94931, status=500
INFO[0312] ❌ userId=91718, status=500

#### 3-5. 처리량(Throughput) 분석
실제로는 TPS 300이 아니라 단계적 증가(Ramping) 시나리오로 구현
1분: 50 TPS
1분: 100 TPS
1분: 200 TPS
2분: 300 TPS (목표)
1분: 50 TPS (쿨다운)
평균 158 TPS = 정상적인 결과로 보임

#### 3-6. VU(Virtual User) 효율성
TPS 50:  최대 10 VU 할당 → 실제 2 VU 사용 (20%)
TPS 100: 최대 20 VU 할당 → 실제 3 VU 사용 (15%)
TPS 300: 최대 50 VU 할당 → 실제 30 VU 사용 (60%)


#### 그래프
- 그래프는 document/result/coupon-tps/부하_테스트_결과_TPS별_Latency_및_실패율_비교_그래프.png로 따로 보관

- Latency 비교 그래프
Latency (ms)
800 |                                      
700 |                                      ●(max 6.58s 제외)
600 |                                      
500 |------------------------목표선---------
400 |                                      
300 |                                      
200 |                              ●(p99)  
100 |          ●(p99)         ●(p95)       
50  |  ●(p95)  ●(p95)                      
0   |__●●●______●●●______________●●●______
      TPS 50    TPS 100        TPS 300

● avg(평균)  ● med(중앙값)  ● p95  ● p99

- 실패율 비교 그래프
실패율 비교 그래프
Error Rate (%)
1.0% |------------------------목표선---------
0.8% |
0.6% |                              ▓(0.69%)
0.4% |
0.2% |          ▓(0.12%)
0.0% |  ▓(0.01%)
|___________________________________
TPS 50   TPS 100    TPS 300

#### 병목 지점 확인
- TPS 300에서 일부 느린 지점 확인
p95: 106ms (양호)
p99: 718ms (약간 높음)
max: 6.58s (매우 높음)

-> 원인 추정: 낙관적 락 및 재시도 인한 지연, Redis Lock 타임아웃 시간
==============================
- 낙관적 락 재시도 로그에서 확인 내역
!!낙관적 락 충돌 (재시도 6/10): userId=109032, couponId=1, error=Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [kr.hhplus.be.server.infrastructure.coupon.entity.CouponJpaEntity#1]
1000ms 대기 후 재시도...
==============================

- 500 에러 6건에 대한 원인 추정 필요
-> 로그 확인 필요(이전 테스트에서는 이미 발급 받은 쿠폰 에러 인데 TPS300에서만 난 것으로 보아 재테스트 및 재확인 필요)

#### 개선 전략
1. 커넥션 풀 증가
2. 낙관적 락 재시도 횟수 및 대기시간 감소
3. Redis lock 타임아웃 조정(현재 5000, InventoryService)
