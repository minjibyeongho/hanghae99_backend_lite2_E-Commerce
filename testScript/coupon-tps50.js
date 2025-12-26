import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { vu } from 'k6/execution';

const errorRate = new Rate('errors');

// ✅ TPS 50 설정
export const options = {
    scenarios: {
        constant_rate_50: {
            executor: 'constant-arrival-rate',
            rate: 50,              // 초당 50개 요청
            timeUnit: '1s',
            duration: '3m',        // 3분간 실행
            preAllocatedVUs: 10,   // 미리 준비할 VU 수
            maxVUs: 50,            // 최대 VU 수
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],  // 95%는 500ms, 99%는 1초 이내
        http_req_failed: ['rate<0.01'],                  // 실패율 1% 미만
        errors: ['rate<0.01'],
    },
};

export default function () {
    const url = 'http://localhost:8080/api/coupons/issue';

    // 고유한 userId 생성
    const userId = vu.idInTest + (vu.iterationInScenario * 100);

    const payload = JSON.stringify({
        userId: userId,
        couponId: 1,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const response = http.post(url, payload, params);

    // 실패 시 로그
    if (response.status !== 200) {
        console.log(`❌ userId=${userId}, status=${response.status}, body=${response.body}`);
    }

    const success = check(response, {
        'status is 200': (r) => r.status === 200,
        'response has userCouponId': (r) => {
            try {
                return JSON.parse(r.body).userCouponId !== undefined;
            } catch (e) {
                return false;
            }
        },
        'p95 < 500ms': (r) => r.timings.duration < 500,
        'p99 < 1000ms': (r) => r.timings.duration < 1000,
    });

    errorRate.add(!success);

    // Think Time 제거 (높은 부하 생성)
    // sleep(1);
}
