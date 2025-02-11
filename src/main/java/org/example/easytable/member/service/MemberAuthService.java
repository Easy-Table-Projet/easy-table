package org.example.easytable.member.service;

import java.util.Optional;

import org.example.easytable.common.exception.CustomException;
import org.example.easytable.common.exception.ErrorCode;
import org.example.easytable.common.utills.JwtUtil;
import org.example.easytable.member.dto.request.MemberSignInReqDto;
import org.example.easytable.member.dto.request.MemberSignUpReqDto;
import org.example.easytable.member.dto.response.MemberSignInResDto;
import org.example.easytable.member.dto.response.MemberSignUpResDto;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder 사용

	// 멤버 회원가입 로직
	public MemberSignUpResDto signUp(MemberSignUpReqDto requestDto) {

		// 등록된 이메일 여부 확인
		Optional<Member> existingMember = memberRepository.findByEmail(requestDto.getEmail());

		// 존재하면 예외 처리
		if (existingMember.isPresent()) {
			log.info("이미 존재하는 이메일입니다. 이메일: {}", requestDto.getEmail());
			throw new CustomException(ErrorCode.EMAIL_EXISTS);
		}

		// 사용자가 입력한 비밀번호를 암호화
		String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

		// Member 객체 생성, 이메일과 암호화된 비밀번호를 설정
		Member newMember = Member.builder()
			.email(requestDto.getEmail())
			.name(requestDto.getName())
			.password(encryptedPassword)
			.address(requestDto.getAddress())
			.userType(Member.UserType.USER)
			.isDeleted(false)
			.build();

		// DB에 Member 저장
		Member savedMember = memberRepository.save(newMember);

		// savedMember 반환
		return new MemberSignUpResDto(savedMember);
	}

	// 로그인
	public MemberSignInResDto signIn(MemberSignInReqDto requestDto) {

		// 1. 이메일을 기반으로 사용자 찾기
		Member member = memberRepository.findByEmail(requestDto.getEmail())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));  // 이메일로 멤버 찾고 없으면 예외 발생

		// 2. 비밀번호 일치 여부 확인
		boolean passwordMatches = passwordEncoder.matches(requestDto.getPassword(), member.getPassword());
		if (!passwordMatches) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD);  // 비밀번호 불일치 시 예외 처리
		}

		// 3. JWT 토큰 생성 (member.getId()는 Long, member.getEmail()은 String)
		String token = jwtUtil.createToken(member.getId(), member.getEmail());  // 아이디와 이메일을 기반으로 JWT 토큰 생성

		// 4. JWT 토큰을 포함한 응답 반환 (응답에 토큰을 반환)
		return new MemberSignInResDto(token);  // 생성된 토큰을 응답에 포함시킴
	}

	// 회원 탈퇴
	public void resign(String token, String password) {
		Long memberId = jwtUtil.getMemberIdFromToken(token);
		Member member = memberService.findMemberById(memberId);

		if (member.isDeleted()) {
			throw new CustomException(ErrorCode.ALREADY_DELETED_USER);
		}

		if (!passwordEncoder.matches(password, member.getPassword())) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD);
		}

		member.inActivate();
		memberRepository.save(member);

		log.info("사용자가 탈퇴했습니다. memberId: {}", memberId);
	}
}