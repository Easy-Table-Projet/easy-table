package org.example.easytable.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.easytable.auth.dto.request.LoginReqDto;
import org.example.easytable.auth.dto.request.RegisterReqDto;
import org.example.easytable.auth.dto.response.RegisterResDto;
import org.example.easytable.auth.service.AuthService;
import org.example.easytable.member.entity.Member;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginReqDto loginReqDto) {
        // AuthService를 통해 인증 및 토큰 생성
        String token = authService.authenticate(loginReqDto);

        // Authorization 헤더에 토큰 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        // 헤더만 포함하고 본문은 비움
        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }

    @PostMapping("/signup")
    public ResponseEntity<RegisterResDto> registerUser(@Valid @RequestBody RegisterReqDto registerReqDto) {
        // 회원가입 처리
        Member member = authService.registerMember(registerReqDto);

        // 응답 반환
        RegisterResDto response = RegisterResDto.of(member.getEmail());

        return ResponseEntity.ok(response);
    }
}

