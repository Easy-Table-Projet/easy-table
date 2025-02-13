package org.example.easytable.common.config;

import org.example.easytable.common.filter.JwtFilter;
import org.example.easytable.common.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtUtil jwtUtil;

	// JwtUtil을 생성자로 주입받습니다.
	public SecurityConfig(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Bean(name = "securityJwtFilter")
	public JwtFilter jwtFilter() {
		return new JwtFilter(jwtUtil);  // JwtFilter 빈 생성
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())  // CSRF 비활성화
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/members/sign-up", "/api/members/sign-in").permitAll() // 회원가입 & 로그인은 인증 없이 허용
				.anyRequest().authenticated() // 그 외 요청은 인증 필요
			)
			.formLogin(form -> form.disable()) // 기본 로그인 폼 비활성화
			.httpBasic(httpBasic -> httpBasic.disable()) // HTTP 기본 인증 비활성화
			.addFilterBefore(jwtFilter(), SecurityContextHolderAwareRequestFilter.class); // JwtFilter를 인증 필터로 추가

		return http.build();
	}

	// UserDetailsService 자동 설정 방지
	@Bean
	public UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager(); // 빈 설정을 강제하여 자동 설정 방지
	}

	// AuthenticationManager 수동 등록 (Spring Security 기본 설정 방지)
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}