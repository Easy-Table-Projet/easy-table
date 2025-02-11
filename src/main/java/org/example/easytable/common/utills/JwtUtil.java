package org.example.easytable.common.utills;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.*;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtil {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTH_HEADER = "Authorization";

	@Value("${jwt.secret}") // application.yml 또는 .properties에서 불러오기
	private String secretKey;

	private final long TOKEN_TIME = 60 * 60 * 1000L; // 1시간 (밀리초 단위)

	// 🔹 JWT 토큰 생성
	public String createToken(Long memberId, String email) {
		return BEARER_PREFIX +
			Jwts.builder()
				.setSubject(email)
				.claim("memberId", memberId)
				.claim("role", "ROLE_MEMBER")
				.setExpiration(new Date(System.currentTimeMillis() + TOKEN_TIME))
				.signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
				.compact();
	}

	// 🔹 요청 헤더에서 JWT 토큰 추출
	public String extractToken(HttpServletRequest request) {
		String authHeader = request.getHeader(AUTH_HEADER);

		if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
			return authHeader.substring(BEARER_PREFIX.length()); // "Bearer " 부분 제거 후 반환
		}
		return null; // 토큰이 없으면 null 반환
	}

	// 🔹 JWT 토큰 유효성 검사
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
				.build()
				.parseClaimsJws(token);
			return true; // 유효한 토큰이면 true 반환
		} catch (ExpiredJwtException e) {
			System.out.println("만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			System.out.println("지원되지 않는 JWT 토큰 형식입니다.");
		} catch (MalformedJwtException e) {
			System.out.println("잘못된 JWT 토큰 형식입니다.");
		} catch (SignatureException e) {
			System.out.println("JWT 서명이 유효하지 않습니다.");
		} catch (Exception e) {
			System.out.println("JWT 토큰 검증 중 오류 발생: " + e.getMessage());
		}
		return false;
	}

	// 🔹 토큰에서 `memberId` 추출
	public Long getMemberIdFromToken(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
				.build()
				.parseClaimsJws(token)
				.getBody()
				.get("memberId", Long.class);
		} catch (Exception e) {
			System.out.println("JWT에서 memberId를 추출하는 중 오류 발생: " + e.getMessage());
			return null;
		}
	}
}