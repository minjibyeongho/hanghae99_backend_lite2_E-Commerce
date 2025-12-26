# 부하 테스트

## 0. 진행 순서
- 테스트 API 선정 -> k6 설치 -> k6로 부하테스트 진행 -> 부하테스트 관련 보고서 작성

## 1. 테스트 API 선정

### 1-1. 쿠폰 발급 API (POST /api/coupons/issue)
- 동시성 이슈가 가장 심각한 지점
- Optimistic Lock과 Retry 로직이 구현되어 있어 부하 테스트 효과가 명확합니다
- 선착순 이벤트 시나리오를 시뮬레이션할 수 있습니다

### 1-2. 주문 생성 API (POST /api/v1/orders)
- 재고 예약, 지갑 결제, 주문 생성 등 여러 트랜잭션이 연계됩니다
- Redis 분산 락을 사용한 재고 관리 로직이 있어 성능 병목 지점 파악에 적합합니다

### 1-3. 상품 조회 API (GET /api/v1/products/{productId})
- Redis 캐싱 전략이 적용되어 있습니다
- 읽기 트래픽 증가 시 캐시 효율성 검증이 가능합니다

## 2. k6 설치
- mac에 brew활용 설치 완료

## 3. 샘플 js 작성 및 테스트
- testScript/coupon-load-test.js 작성 및 테스트 진행
- 내용이 길어서 따로 파일로 작성( coupon-load-test-result.md )
- 요약: 테스트 성공

## 4. tps50, 100, 300 테스트


