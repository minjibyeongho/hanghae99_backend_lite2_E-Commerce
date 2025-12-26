import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { vu } from 'k6/execution';

// 커스텀 메트릭: 에러율 추적
const errorRate = new Rate('errors');

// 기본 테스트 옵션 (아직 부하는 적게)
export const options = {
    vus: 5,              // 가상 사용자 5명
    duration: '30s',     // 30초 동안 실행

    // 성능 목표 설정
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 이내
        errors: ['rate<0.01'],               // 에러율 1% 미만
    },
};

export default function () {
    // API 엔드포인트
    const url = 'http://localhost:8080/api/coupons/issue';

    // 각 VU가 고유한 userId 사용
    const userId = vu.idInTest + (vu.iterationInScenario * 5);

    const payload = JSON.stringify({
        userId: userId,
        couponId: 1,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // POST 요청 실행
    const response = http.post(url, payload, params);

    // 🔍 실패한 응답 내용을 출력
    if (response.status !== 200) {
        console.log(`❌ 실패! Status: ${response.status}`);
        console.log(`Response Body: ${response.body}`);
        console.log(`User ID: ${JSON.parse(payload).userId}`);
    }

    // 응답 검증
    const success = check(response, {
        'status is 200': (r) => r.status === 200,
        'response has userCouponId': (r) => JSON.parse(r.body).userCouponId !== undefined,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    // 에러율 기록
    errorRate.add(!success);

    // 사용자가 다음 요청 전 대기하는 시간 (Think Time)
    sleep(1);
}
