import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { vu } from 'k6/execution';

const errorRate = new Rate('errors');

// ✅ TPS 100 설정
export const options = {
    scenarios: {
        constant_rate_100: {
            executor: 'constant-arrival-rate',
            rate: 100,             // 초당 100개 요청
            timeUnit: '1s',
            duration: '3m',
            preAllocatedVUs: 20,
            maxVUs: 100,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
        errors: ['rate<0.01'],
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
        'p95 < 500ms': (r) => r.timings.duration < 500,
        'p99 < 1000ms': (r) => r.timings.duration < 1000,
    });

    errorRate.add(!success);
}
