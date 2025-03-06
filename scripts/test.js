import http from 'k6/http';
import { check, sleep } from 'k6';

// 부하 테스트 설정
export const options = {
    stages: [
        { duration: '1m', target: 100 },  // 1분 동안 100명 유저로 증가
        { duration: '1m', target: 200 },  // 2분까지 200명 유저로 증가
        { duration: '1m', target: 300 },  // 3분까지 300명 유저로 증가
        { duration: '1m', target: 400 },  // 4분까지 400명 유저로 증가
        { duration: '1m', target: 500 },  // 5분까지 500명 유저로 증가
        { duration: '1m', target: 600 },  // 6분까지 600명 유저로 증가
        { duration: '1m', target: 700 },  // 7분까지 700명 유저로 증가
        { duration: '1m', target: 800 },  // 8분까지 800명 유저로 증가
        { duration: '1m', target: 900 },  // 9분까지 900명 유저로 증가
        { duration: '1m', target: 1000 }, // 10분까지 1000명 유저로 증가
    ],
};

const BASE_URL = 'http://localhost:8080';

// 회원가입 → 로그인 → 예약
export default function () {
    // 1️⃣ 회원가입
    const email = `testuser${Math.floor(Math.random() * 1000000)}@example.com`;
    const password = 'password123';

    const signupRes = http.post(`${BASE_URL}/api/auth/signup`, JSON.stringify({
        email: email,
        password: password,
        memberType: 'USER'
    }), { headers: { 'Content-Type': 'application/json' } });

    check(signupRes, {
        '회원가입 성공 여부': (res) => res.status === 200 || res.status === 201
    });

    sleep(1); // 1초 대기


    // 2️⃣ 로그인 요청
    const loginRes = http.post(`${BASE_URL}/api/auth/signin`, JSON.stringify({
        email: email,
        password: password
    }), { headers: { 'Content-Type': 'application/json' } });

    check(loginRes, {
        '로그인 성공 여부': (res) => res.status === 200
    });

    if (loginRes.status !== 200) {
        console.error(`로그인 실패: ${email}, 응답: ${JSON.stringify(loginRes.body)}`);
        return;
    }

    // 응답의 헤더에서 JWT 토큰 추출
    const authToken = loginRes.headers['Authorization'] ? loginRes.headers['Authorization'].split(' ')[1] : null;

    if (!authToken) {
        console.error(`로그인 실패: ${email}, 토큰 없음`);
        return;
    }

    sleep(1); // 1초 대기

    // 3️⃣ 예약 요청
    const reservationTime = '2025-03-01T19:00:00';
    const reservationRes = http.post(`${BASE_URL}/api/reservations/1`, JSON.stringify({
        reservationTime: reservationTime
    }), {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`
        }
    });

    const isReservationSuccess = check(reservationRes, {
        '예약 성공 여부': (res) => res.status === 200 || res.status === 201
    });

    if (isReservationSuccess) {
        console.log(`예약 성공: ${email}, 예약시간: ${reservationTime}`);
    } else {
        console.error(`예약 실패: ${email}, 예약시간: ${reservationTime}, 상태 코드: ${reservationRes.status}`);
    }

    sleep(1); // 1초 대기
}