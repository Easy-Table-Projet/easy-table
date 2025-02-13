package org.example.easytable.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

	public static final String BEARER_PREFIX = "Bearer ";

	@Value("${jwt.secret-key}")
	private String secretKey;

	private final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

	public String createToken(Long memberId, String email) {
		Date date = new Date();
		String memberRole = "ROLE_USER";

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