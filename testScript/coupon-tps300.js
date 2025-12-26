import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { vu } from 'k6/execution';

const errorRate = new Rate('errors');

// ✅ TPS 300 설정 (단계적 증가)
export const options = {
    scenarios: {
        stress_test_300: {
            executor: 'ramping-arrival-rate',
            startRate: 50,
            timeUnit: '1s',
            preAllocatedVUs: 50,
            maxVUs: 300,
            stages: [
                { duration: '1m', target: 50 },   // 1분간 50 TPS (워밍업)
                { duration: '1m', target: 100 },  // 1분간 100 TPS로 증가
                { duration: '1m', target: 200 },  // 1분간 200 TPS로 증가
                { duration: '2m', target: 300 },  // 2분간 300 TPS 유지 (피크)
                { duration: '1m', target: 50 },   // 1분간 50 TPS로 감소 (쿨다운)
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000', 'p(99)<2000'],  // 목표 완화
        http_req_failed: ['rate<0.05'],                   // 실패율 5% 미만
        errors: ['rate<0.05'],
    },
};

export default function () {
    const url = 'http://localhost:8080/api/coupons/issue';

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

    if (response.status !== 200) {
        console.log(`❌ userId=${userId}, status=${response.status}`);
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
        'p95 < 1000ms': (r) => r.timings.duration < 1000,
        'p99 < 2000ms': (r) => r.timings.duration < 2000,
    });

    errorRate.add(!success);
}
