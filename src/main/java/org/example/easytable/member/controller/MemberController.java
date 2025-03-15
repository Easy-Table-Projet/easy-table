package org.example.easytable.member.controller;

import org.example.easytable.member.dto.response.MeResDto;
import org.example.easytable.common.utils.AuthUtil;
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


	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		memberService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

}

