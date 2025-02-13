package org.example.easytable.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.easytable.utils.JwtUtil;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");

		if (!isValidAuthorizationHeader(authorizationHeader)) {
			filterChain.doFilter(request, response);
			return;
		}

		String jwt = authorizationHeader.substring(7);

		try {
			String email = jwtUtil.extractEmail(jwt);
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);
			setAuthentication(request, userDetails);
		} catch (Exception e) {
			filterChain.doFilter(request, response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isValidAuthorizationHeader(String header) {
		return header != null && header.startsWith("Bearer ");
	}

	private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
				);
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}