package org.example.easytable.member.controller;

import org.example.easytable.member.dto.response.MemberGetResDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.example.easytable.common.filter.JwtFilter;
import org.example.easytable.common.utils.JwtUtil;
import org.example.easytable.member.dto.request.MemberUpdateReqDto;
import org.example.easytable.member.dto.response.MemberUpdateResDto;
import org.example.easytable.member.service.MemberService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final JwtUtil jwtUtil;
	// 유저 단건 조회
	@GetMapping("/{memberId}")
	public ResponseEntity<MemberGetResDto> getMember(
		@PathVariable Long memberId,
		HttpServletRequest request
	) {
		// request 헤더에 담긴 토큰을 추출해서 담기
		String token = JwtFilter.extractToken(request);

		// 추출한 토큰으로부터 유저 아이디를 찾기
		Long userIdFromToken = jwtUtil.getMemberIdFromToken(token); // 인스턴스 메서드 호출

		// 유저 정보 조회
		MemberGetResDto responseDto = memberService.getMemberById(memberId, userIdFromToken);

		return new ResponseEntity<>(responseDto, HttpStatus.OK);
	}

	// 유저 수정
	@PatchMapping("/{memberId}")
	public MemberUpdateResDto updateMember(
		@PathVariable Long memberId,
		@RequestBody MemberUpdateReqDto updateUserRequestDto,
		HttpServletRequest request
	) {
		// request헤더에 담긴 토큰을 추출해서 담기
		String token = JwtFilter.extractToken(request);

		// 그렇게 담은 추출한 토큰으로부터 유저아이디를 찾기
		Long userIdFromToken = jwtUtil.getMemberIdFromToken(token); // 인스턴스 메서드 호출

		return memberService.updateMember(memberId, updateUserRequestDto, userIdFromToken);
	}
}
