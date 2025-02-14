package org.example.easytable.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.auth.dto.request.LoginReqDto;
import org.example.easytable.auth.dto.request.RegisterReqDto;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.entity.MemberType;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public String authenticate(LoginReqDto loginReqDto) {
        // 사용자 정보 로드
        Member member = memberRepository.findByEmail(loginReqDto.getEmail())
                .orElseThrow(() -> CustomException.of(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 잘못되었습니다."));

        // 탈퇴한 회원 검증
        if (member.isDeleted()) {
            throw CustomException.of(ErrorCode.UNAUTHORIZED, "탈퇴한 회원입니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(loginReqDto.getPassword(), member.getPassword())) {
            throw CustomException.of(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // JWT 토큰 생성
        return jwtUtil.generateToken(member.getId(), member.getEmail(), member.getMemberType().getAuthority());
    }

    public Member registerMember(RegisterReqDto registerReqDto) {
        // Role 값 검증 및 변환
        if (!MemberType.isValid(registerReqDto.getMemberType())) {
            throw CustomException.of(
                    ErrorCode.BAD_REQUEST,
                    String.format("Invalid member type: '%s'. Allowed values are: %s",
                            registerReqDto.getMemberType(),
                            Arrays.toString(MemberType.values()))
            );
        }
        MemberType memberType = MemberType.valueOf(registerReqDto.getMemberType().toUpperCase());

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(registerReqDto.getEmail())) {
            throw CustomException.of(ErrorCode.CONFLICT,
                    "이미 사용 중인 이메일입니다: " + registerReqDto.getEmail());
        }

        // 이메일에서 @ 이전의 값 추출
        String name = registerReqDto.getEmail().split("@")[0];

        // 사용자 생성 및 저장
        Member user = Member.builder()
                .name(name) // 이메일의 @ 앞 부분을 이름으로 사용
                .email(registerReqDto.getEmail())
                .password(passwordEncoder.encode(registerReqDto.getPassword()))
                .memberType(memberType)
                .build();

        return memberRepository.save(user);
    }

}
