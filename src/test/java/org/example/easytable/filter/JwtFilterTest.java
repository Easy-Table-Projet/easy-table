package org.example.easytable.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.easytable.common.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtFilterTest {

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@Mock
	private UserDetails userDetails;

	@InjectMocks
	private JwtFilter jwtFilter;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		SecurityContextHolder.clearContext(); // 매 테스트 실행 전 SecurityContext 초기화
	}

	@Test
	void testDoFilterInternal_ValidToken() throws ServletException, IOException {
		String token = "valid.jwt.token";
		String email = "user@example.com";

		when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
		when(jwtUtil.extractEmail(token)).thenReturn(email);
		when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
		when(userDetails.getAuthorities()).thenReturn(Collections.emptyList()); // 추가

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(userDetailsService).loadUserByUsername(email);
		verify(filterChain).doFilter(request, response);
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		verify(userDetails).getAuthorities(); // 추가
	}

	@Test
	void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
		String token = "invalid.jwt.token";

		when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
		when(jwtUtil.extractEmail(token)).thenThrow(new RuntimeException("Invalid Token"));

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void testDoFilterInternal_NoAuthorizationHeader() throws ServletException, IOException {
		when(request.getHeader("Authorization")).thenReturn(null);

		jwtFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
}