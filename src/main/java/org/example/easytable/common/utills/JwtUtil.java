package org.example.easytable.common.utills;

import java.util.Date;

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Getter
public class JwtUtil {

	// JWT 토큰 값 앞에 붙는 접두사
	public static final String BEARER_PREFIX = "Bearer ";

	private static final String secretKey = "testestestdfns214nkdc2141mkse51s31512tfadsds";  // 비밀 키

	private final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

	private final AbstractErrorController abstractErrorController;

	public JwtUtil(AbstractErrorController abstractErrorController) {
		this.abstractErrorController = abstractErrorController;
	}

	// JWT 토큰 생성
	public String createToken(Long memberId, String email) {
		Date date = new Date();

		// 권한부여 : role 기본값을 'MEMBER'로 설정
		String memberRole = ("ROLE_MEMBER");

		return BEARER_PREFIX +
			Jwts.builder()
				.setSubject(email)  // 사용자 식별자 (이메일)
				.claim("memberId", memberId)  // 'memberId'를 Long 타입으로 클레임에 추가
				.claim("role", memberRole)  // 사용자 역할을 클레임에 추가
				.setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 토큰의 만료시간
				.setIssuedAt(date) // 토큰 발급 시점
				.signWith(SignatureAlgorithm.HS256, secretKey) // 알고리즘과 시크릿 키
				.compact();
	}

	// JWT 토큰에서 memberId 추출
	public static Long getMemberIdFromToken(String token) {
		return Jwts.parser()
			.setSigningKey(secretKey)
			.parseClaimsJws(token)
			.getBody()
			.get("memberId", Long.class);  // memberId를 Long 타입으로 추출
	}

	// 토큰 유효성 검사
	public boolean validateToken(String token) {
		try {
			// JWT 파서 빌더를 사용하여 토큰의 서명을 검증
			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token); // 토큰 파싱 및 검증
			return true; // 토큰이 유효한 경우

		} catch (SecurityException | MalformedJwtException | SignatureException e) {
			// 토큰 서명이 잘못되었더나, 잘못된 형식의 JWT가 전달된 경우
			log.error("유효하지 않는 JWT 서명입니다.", e);
		} catch (ExpiredJwtException e) {
			// 토크이 만료된 경우
			log.error("만료된 JWT Token 입니다.");
		} catch (UnsupportedJwtException e) {
			// 지원되지 않는 JWT 형식이 전달된 경우
			log.error("지원되지 않는 JWT 토큰입니다.", e);
		} catch (IllegalArgumentException e) {
			// JWT 클레임이 비어있거나 잘못된 형식일 경우
			log.error("잘못된 JWT 토큰입니다.", e);
		}

		// 토큰이 유효하지 않은 경우
		return false;
	}
}
