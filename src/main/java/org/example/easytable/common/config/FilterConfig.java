package org.example.easytable.common.config;

import org.example.easytable.common.filter.JwtFilter;
import org.example.easytable.common.utills.JwtUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

	private final JwtUtil jwtUtil;  // JwtUtil 주입

	public FilterConfig(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Bean
	public FilterRegistrationBean<JwtFilter> jwtFilter() {
		FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();

		// JwtFilter 인스턴스 생성 후 주입
		registrationBean.setFilter(new JwtFilter(jwtUtil));  // JwtUtil을 필터에 주입

		registrationBean.addUrlPatterns("/api/*");  // "api/"로 시작하는 모든 URL에 대해 필터를 적용
		registrationBean.setOrder(1);  // 필터 우선순위 설정

		return registrationBean;
	}
}
