package org.example.easytable.member.controller;

import org.example.easytable.member.dto.response.MeResDto;
import org.example.easytable.utils.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.easytable.member.service.MemberService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;


	@GetMapping("/me")
	public ResponseEntity<MeResDto> getAuthenticatedUserInfo() {
		// 인증된 사용자의 ID와 역할 목록을 가져옵니다.
		Long userId = AuthUtil.getId();
		List<String> userRoles = AuthUtil.getRoles();

		// DTO를 사용하여 응답 반환
		MeResDto response = MeResDto.of(userId, userRoles);

		return ResponseEntity.ok(response);
	}


	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
		memberService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

}

