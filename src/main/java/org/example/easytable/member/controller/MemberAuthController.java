package org.example.easytable.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.example.easytable.common.filter.JwtFilter;
import org.example.easytable.member.dto.request.MemberResignReqDto;
import org.example.easytable.member.dto.request.MemberSignInReqDto;
import org.example.easytable.member.dto.request.MemberSignUpReqDto;
import org.example.easytable.member.dto.response.MemberResignResDto;
import org.example.easytable.member.dto.response.MemberSignInResDto;
import org.example.easytable.member.dto.response.MemberSignUpResDto;
import org.example.easytable.member.service.MemberAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor

public class MemberAuthController {
	private final MemberAuthService memberAuthService;

	// 유저 회원가입
	@PostMapping("/sign-up")
	public ResponseEntity<MemberSignUpResDto> signUpUser(
		@RequestBody MemberSignUpReqDto requestDto
	) {
		MemberSignUpResDto responseDto = memberAuthService.signUp(requestDto);

		return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
	}

	// 유저 로그인
	@PostMapping("/sign-in")
	public ResponseEntity<MemberSignInResDto> SignInUser(
		@RequestBody MemberSignInReqDto requestDto
	) {
		MemberSignInResDto responseDto = memberAuthService.signIn(requestDto);

		return new ResponseEntity<>(responseDto, HttpStatus.OK);
	}

	// 유저 탈퇴
	@DeleteMapping("/{memberId}")
	public ResponseEntity<Void> deleteMember(
		@PathVariable Long memberId,
		HttpServletRequest request
	) {
		// Authorization 헤더에서 JWT 토큰 추출
		String token = JwtFilter.extractToken(request);

		// 회원 삭제 서비스 호출
		memberAuthService.deleteMember(memberId, token);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}