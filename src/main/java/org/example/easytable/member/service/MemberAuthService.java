package org.example.easytable.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.example.easytable.member.entity.Member;
import org.example.easytable.common.exception.CustomException;
import org.example.easytable.common.exception.ErrorCode;
import org.example.easytable.common.utils.JwtUtil;
import org.example.easytable.member.dto.request.MemberSignInReqDto;
import org.example.easytable.member.dto.request.MemberSignUpReqDto;
import org.example.easytable.member.dto.response.MemberSignInResDto;
import org.example.easytable.member.dto.response.MemberSignUpResDto;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.common.utils.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final JwtUtil jwtUtil;
	PasswordEncoder bcrypt = new PasswordEncoder();

	// 유저 회원가입 로직
	public MemberSignUpResDto signUp(MemberSignUpReqDto requestDto) {

		// 등록된 이메일 여부 확인
		Optional<Member> existingMember = memberRepository.findByEmail(requestDto.getEmail());

		// 존재하면 예외 처리
		if (existingMember.isPresent()) {
			log.info("이미 존재하는 이메일입니다. 이메일: {}", requestDto.getEmail());
			throw new CustomException(ErrorCode.EMAIL_EXISTS);
		}

		// 사용자가 입력한 비밀번호를 암호화
		String encryptedPassword = bcrypt.encode(requestDto.getPassword());

		// Member 객체 생성, 이메일과 암호화된 비밀번호를 설정
		Member newMember = new Member(requestDto.getEmail(), requestDto.getMembername(), encryptedPassword);

		// DB에 Member 저장
		Member savedMember = memberRepository.save(newMember);

		// savedUser 반환
		return new MemberSignUpResDto(savedMember);
	}

	public MemberSignInResDto signIn(MemberSignInReqDto requestDto) {

		// 1. 이메일을 기반으로 사용자 찾기
		Member member = memberRepository.findByEmail(requestDto.getEmail())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));  // 이메일로 유저 찾고 없으면 예외 발생

		// 2. 비밀번호 일치 여부 확인
		boolean passwordMatches = bcrypt.matches(requestDto.getPassword(), member.getPassword());
		if (!passwordMatches) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD);  // 비밀번호 불일치 시 예외 처리
		}

		// 3. JWT 토큰 생성 (member.getId()는 Long, member.getEmail()은 String)
		String token = jwtUtil.createToken(member.getId(), member.getEmail());  // 아이디와 이메일을 기반으로 JWT 토큰 생성

		// 4. JWT 토큰을 포함한 응답 반환 (응답에 토큰을 반환)
		return new MemberSignInResDto(token);  // 생성된 토큰을 응답에 포함시킴
	}

	public void resign(String token, String password) {

		// 1. JWT 토큰에서 userId 추출
		Long memberId = jwtUtil.getMemberIdFromToken(token);  // 멤버Id로 변경

		// 2. 비밀번호 확인
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));  // memberRepository 직접 사용하여 조회

		boolean passwordMatches = bcrypt.matches(password, member.getPassword());
		if (!passwordMatches) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD);  // 비밀번호가 일치하지 않으면 예외 발생
		}

		// 3. 탈퇴 처리: 사용자의 상태를 "탈퇴"로 변경
		member.inActivate();  // 'active' 플래그를 false로 설정하여 탈퇴 처리
		memberRepository.save(member);  // DB에 반영

		log.info("사용자가 탈퇴했습니다. userId: {}", memberId);
	}

}