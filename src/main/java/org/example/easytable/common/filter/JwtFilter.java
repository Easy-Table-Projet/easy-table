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

	private static final String[] SIGN_UP_URI = {
		"/api/auth/members/signup"
	};

	private static final String[] SIGN_IN_URI = {
		"/api/auth/members/signin"
	};

	private static final String[] MEMBER_SEARCH_URI = {
		"/api/auth/members/search"
	};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// 클라이언트가 요청한 URI
		String requestURI = httpRequest.getRequestURI();

		// 회원가입 또는 로그인 URI일 경우, JWT 인증 없이 그냥 필터를 통과시킴
		if (isSignUpURI(requestURI) || isSignInURI(requestURI) || isMemberSearchURI(requestURI)) {
			chain.doFilter(request, response);
			return;
		}

		// 1. 요청 헤더에서 토큰 추출
		String token = extractToken(httpRequest);

		// 2. 토큰이 있으면, 유효성 검증
		if (token != null && jwtUtil.validateToken(token)) {

			// 3. 유효한 토큰이면 맴버 아이디 추출
			Long memberId = jwtUtil.getMemberIdFromToken(token);

			// request를 통해 멤버 아이디를 넣어주는 로직 구현
			request.setAttribute("memberId", memberId);

			// 인증 정보 설정
			System.out.println("인증된 사용자 아이디: " + memberId);

		} else {

			// 5. 토큰이 없거나 유효하지 않으면, 인증 실패 처리 (예시로 401 Unauthorized 반환)
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			httpResponse.getWriter().write("유효하지 않은 토큰입니다.");
			return;
		}

		// 6. 필터 체인 계속 진행
		chain.doFilter(request, response);
	}

	// 7. 요청 헤더에서 JWT 토큰 추출하는 메소드
	public static String extractToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			return header.substring(7); // "Bearer "를 제외하고 토큰만 추출
		}
		return null;  // 토큰이 없으면 null 반환
	}

	// requestURI가 회원가입 URI인지 확인
	public boolean isSignUpURI(String requestURI) {
		return PatternMatchUtils.simpleMatch(SIGN_UP_URI, requestURI);
	}

	// requestURI가 로그인 URI인지 확인
	public boolean isSignInURI(String requestURI) {
		return PatternMatchUtils.simpleMatch(SIGN_IN_URI, requestURI);
	}

	// requestURI가 조회 URI인지 확인
	public boolean isMemberSearchURI(String requestURI) {
		return PatternMatchUtils.simpleMatch(MEMBER_SEARCH_URI, requestURI);
	}

}
