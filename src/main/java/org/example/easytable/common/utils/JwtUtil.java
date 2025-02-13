package org.example.easytable.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

	// JWT 토큰 값 앞에 붙는 접두사
	public static final String BEARER_PREFIX = "Bearer ";

	@Value("${jwt.secret-key}")  // application.properties에서 비밀 키 가져오기
	private String secretKey;

	private final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

	private final AbstractErrorController abstractErrorController;

	public JwtUtil(AbstractErrorController abstractErrorController) {
		this.abstractErrorController = abstractErrorController;
	}

	public String createToken(Long memberId, String email) {
		Date date = new Date();

		// 기본 역할을 'ROLE_MEMBER'로 설정
		String memberRole = "ROLE_MEMBER";

		return BEARER_PREFIX +
			Jwts.builder()
				.setSubject(email)
				.claim("memberId", memberId)
				.claim("role", memberRole)
				.setExpiration(new Date(date.getTime() + TOKEN_TIME))
				.setIssuedAt(date)
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();
	}

	public Long getMemberIdFromToken(String token) {
		return Jwts.parser()
			.setSigningKey(secretKey)
			.parseClaimsJws(token)
			.getBody()
			.get("memberId", Long.class);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			log.error("유효하지 않은 JWT 토큰", e);
			return false;
		}
	}
}