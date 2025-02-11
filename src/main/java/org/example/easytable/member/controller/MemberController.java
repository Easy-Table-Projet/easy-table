package org.example.easytable.member.controller;

import org.example.easytable.common.filter.JwtFilter;
import org.example.easytable.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.example.easytable.common.utills.JwtUtil;
import org.example.easytable.member.dto.request.MemberUpdateReqDto;
import org.example.easytable.member.dto.response.MemberUpdateResDto;
import org.example.easytable.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final JwtUtil jwtUtil;

	// 멤버 다건 조회 (이메일과 사용자이름 페이징 처리로 검색한 로직)
	@GetMapping("/search")
	public ResponseEntity<?> searchMembers(
		@RequestParam(required = false, defaultValue = "") String email,
		@RequestParam(required = false, defaultValue = "") String membername,
		Pageable pageable) {
		try {
			Page<Member> members = memberService.searchMatchedMembers(email, membername, pageable);
			return ResponseEntity.ok(members);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 멤버 수정
	@PatchMapping("/{memberId}")
	public ResponseEntity<Object> updateMember(
		@PathVariable Long memberId,
		@RequestBody MemberUpdateReqDto updateMemberRequestDto,
		HttpServletRequest request
	) {
		// 🔹 JwtUtil을 통해 토큰 추출 (JwtFilter에서 직접 가져오는 방식 X)
		String token = jwtUtil.extractToken(request);
		Long memberIdFromToken = jwtUtil.getMemberIdFromToken(token);

		if (!memberId.equals(memberIdFromToken)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body("수정 권한이 없습니다.");
		}

		MemberUpdateResDto responseDto = memberService.updateMember(memberId, updateMemberRequestDto, memberIdFromToken);
		return ResponseEntity.ok(responseDto);
	}
}
