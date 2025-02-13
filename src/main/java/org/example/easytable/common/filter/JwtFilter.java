package org.example.easytable.common.filter;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.PatternMatchUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.easytable.common.utils.JwtUtil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter implements Filter {

	private final JwtUtil jwtUtil;

	private static final String[] SIGN_UP_URI = {
		"/api/members/sign-up"
	};

	private static final String[] SIGN_IN_URI = {
		"/api/members/sign-in"
	};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// 클라이언트가 요청한 URI
		String requestURI = httpRequest.getRequestURI();
		log.info("요청 URI: {}", requestURI);

		// 회원가입 또는 로그인 요청이면 필터 통과
		if (isSignUpURI(requestURI) || isSignInURI(requestURI)) {
			log.info("회원가입 또는 로그인 요청 -> 필터 통과");
			chain.doFilter(request, response);
			return;
		}

		// JWT 토큰 추출
		String token = extractToken(httpRequest);
		log.info("추출된 토큰: {}", token);

		// 토큰 검증
		if (token != null && jwtUtil.validateToken(token)) {
			Long memberId = jwtUtil.getMemberIdFromToken(token);
			// SecurityContext에 인증 정보 설정
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				memberId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
			);
			// 권한 분리 필요
			SecurityContextHolder.getContext().setAuthentication(authentication);  // 인증 정보를 SecurityContext에 설정
			log.info("인증된 사용자 ID: {}", memberId);
		} else {
			log.warn("유효하지 않은 토큰 -> 401 응답 반환");
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			httpResponse.getWriter().write("유효하지 않은 토큰입니다.");
			return;
		}

		// 필터 체인 계속 진행
		chain.doFilter(request, response);
	}

	// 요청 URI가 회원가입 경로인지 확인
	private boolean isSignUpURI(String requestURI) {
		for (String pattern : SIGN_UP_URI) {
			if (PatternMatchUtils.simpleMatch(pattern, requestURI)) {
				return true;
			}
		}
		return false;
	}

	// 요청 URI가 로그인 경로인지 확인
	private boolean isSignInURI(String requestURI) {
		for (String pattern : SIGN_IN_URI) {
			if (PatternMatchUtils.simpleMatch(pattern, requestURI)) {
				return true;
			}
		}
		return false;
	}

	// 요청 헤더에서 JWT 토큰 추출
	public static String extractToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			return header.substring(7); // "Bearer " 제거 후 토큰 반환
		}
		return null;  // 토큰이 없으면 null 반환
	}
}