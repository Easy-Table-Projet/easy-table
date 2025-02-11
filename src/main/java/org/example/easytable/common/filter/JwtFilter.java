package org.example.easytable.common.filter;

import java.io.IOException;

import org.example.easytable.common.utills.JwtUtil;
import org.springframework.util.PatternMatchUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter implements Filter {

	private final JwtUtil jwtUtil;

	// 예외적으로 JWT 검증 없이 허용할 API 경로
	private static final String[] WHITELISTED_URIS = {
		"/api/members/sign-up",
		"/api/members/sign-in"
	};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String requestURI = httpRequest.getRequestURI();
		System.out.println("[JwtFilter] 요청 URI: " + requestURI);

		// 회원가입 & 로그인 요청은 필터를 통과시킴
		if (isWhitelistedURI(requestURI)) {
			System.out.println("[JwtFilter] 인증이 필요 없는 요청이므로 필터 통과: " + requestURI);
			chain.doFilter(request, response);
			return;
		}

		// 1. 요청 헤더에서 토큰 추출
		String token = jwtUtil.extractToken(httpRequest);
		System.out.println("[JwtFilter] 추출된 토큰: " + token);

		// 2. 토큰 검증
		if (token != null && jwtUtil.validateToken(token)) {
			Long memberId = jwtUtil.getMemberIdFromToken(token);
			System.out.println("[JwtFilter] 유효한 토큰 - memberId: " + memberId);
			request.setAttribute("memberId", memberId);
		} else {
			System.out.println("[JwtFilter] 유효하지 않은 토큰 또는 토큰 없음!");
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			httpResponse.setContentType("application/json");
			httpResponse.setCharacterEncoding("UTF-8");
			httpResponse.getWriter().write("{\"message\": \"유효하지 않은 토큰입니다.\", \"error\": \"Unauthorized\"}");
			return;
		}

		// 3. 필터 체인 계속 진행
		chain.doFilter(request, response);
	}

	// 인증이 필요 없는 URI인지 확인
	private boolean isWhitelistedURI(String requestURI) {
		return PatternMatchUtils.simpleMatch(WHITELISTED_URIS, requestURI);
	}
}