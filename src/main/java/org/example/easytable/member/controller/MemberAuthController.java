package org.example.easytable.member.controller;

import org.example.easytable.common.utills.JwtUtil;
import org.example.easytable.member.dto.request.MemberSignInReqDto;
import org.example.easytable.member.dto.request.MemberSignUpReqDto;
import org.example.easytable.member.dto.response.MemberResignResDto;
import org.example.easytable.member.dto.response.MemberSignInResDto;
import org.example.easytable.member.dto.response.MemberSignUpResDto;
import org.example.easytable.member.service.MemberAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members") // URI를 명세와 일치하도록 수정
public class MemberAuthController {

	private final MemberAuthService memberAuthService;
	private final JwtUtil jwtUtil;

	// 멤버 회원가입
	@PostMapping("/sign-up")
	public ResponseEntity<?> signUpMember(@RequestBody MemberSignUpReqDto requestDto) {
		try {
			MemberSignUpResDto responseDto = memberAuthService.signUp(requestDto);
			return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 멤버 로그인
	@PostMapping("/sign-in")
	public ResponseEntity<?> signInMember(@RequestBody MemberSignInReqDto requestDto) {
		try {
			MemberSignInResDto responseDto = memberAuthService.signIn(requestDto);
			return ResponseEntity.ok(responseDto);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	// 멤버 탈퇴
	@DeleteMapping("/resign")
	public ResponseEntity<?> resignMember(HttpServletRequest request, @RequestParam String password) {
		try {
			// JWT 토큰 추출
			String token = jwtUtil.extractToken(request);
			if (token == null || !jwtUtil.validateToken(token)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
			}

			// 토큰에서 멤버 ID 추출
			Long memberId = jwtUtil.getMemberIdFromToken(token);

			// `Long` → `String` 변환 후 전달
			memberAuthService.resign(String.valueOf(memberId), password);

			return ResponseEntity.ok(new MemberResignResDto());
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}