package org.example.easytable.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
	private final Key SECRET_KEY;
	private final long TOKEN_VALIDITY;

	public JwtUtil(
			@Value("${jwt.secret:default-secure-key-must-be-32-chars}") String secret,
			@Value("${jwt.token-validity:3600000}") long tokenValidity
	) {
		this.SECRET_KEY = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
		this.TOKEN_VALIDITY = tokenValidity;
	}

	public String generateToken(long id, String email, String memberType) {
		return Jwts.builder()
				.setSubject(email)
				.claim("id", id)
				.claim("memberType", memberType)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
				.signWith(SECRET_KEY, SignatureAlgorithm.HS256)
				.compact();
	}

	public String extractEmail(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(SECRET_KEY)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}