package org.example.easytable.member.service;

import org.example.easytable.member.entity.UserType;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder bcrypt;

	// 유저 회원가입 로직
	@Transactional
	public MemberSignUpResDto signUp(MemberSignUpReqDto requestDto) {
		// userType 기본값을 "USER"로 설정
		String userTypeStr = requestDto.getUserType();
		if (userTypeStr == null || userTypeStr.isBlank()) {
			log.warn("userType이 null 또는 빈 문자열입니다. 기본값 USER로 설정.");
			userTypeStr = "USER";
		}

		UserType userType;
		try {
			userType = UserType.valueOf(userTypeStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.error("유효하지 않은 userType 값: {}", userTypeStr, e);
			throw new CustomException(ErrorCode.INVALID_USER_TYPE, "Invalid userType: " + userTypeStr);
		}

		log.info("이메일 중복 체크 시작: {}", requestDto.getEmail());

		// 이메일 중복 체크
		memberRepository.findByEmail(requestDto.getEmail()).ifPresent(member -> {
			log.info("이미 존재하는 이메일입니다. 이메일: {}", requestDto.getEmail());
			throw new CustomException(ErrorCode.EMAIL_EXISTS);
		});

		log.info("비밀번호 암호화 시작");
		// 비밀번호 암호화
		String encryptedPassword = bcrypt.encode(requestDto.getPassword());

		log.info("Member 객체 생성 시작: {}", requestDto.getEmail());
		// Member 객체 생성
		Member newMember = new Member(
			requestDto.getEmail(),
			requestDto.getMembername(),
			encryptedPassword,
			requestDto.getAddress(),
			userType
		);

		try {
			log.info("DB에 Member 저장 시작");
			// DB에 Member 저장
			Member savedMember = memberRepository.save(newMember);
			log.info("Member 저장 성공");

			// 응답 DTO 반환
			return new MemberSignUpResDto(savedMember);
		} catch (DataIntegrityViolationException e) {
			log.error("데이터 무결성 위반: 이메일 중복 등", e);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "회원 가입 처리 중 오류가 발생했습니다.");
		} catch (Exception e) {
			log.error("회원 가입 중 예외 발생", e);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "회원 가입 처리 중 오류가 발생했습니다.");
		}
	}

	// 회원 로그인
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
		String token;
		try {
			token = jwtUtil.createToken(member.getId(), member.getEmail());
		} catch (Exception e) {
			log.error("JWT 토큰 생성 중 예외 발생", e);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 생성 중 오류가 발생했습니다.");
		}

		// 4. JWT 토큰을 포함한 응답 반환 (응답에 토큰을 반환)
		return new MemberSignInResDto(token);  // 생성된 토큰을 응답에 포함시킴
	}

	// 회원 탈퇴
	@Transactional
	public void deleteMember(Long memberId, String token) {
		// 1. JWT 토큰에서 memberId 추출
		Long userIdFromToken = jwtUtil.getMemberIdFromToken(token);

		// 2. 요청한 memberId와 토큰의 userId가 같은지 확인
		if (!memberId.equals(userIdFromToken)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED, "삭제 권한이 없습니다.");
		}

		// 3. 회원 조회
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 회원입니다."));

		// 4. 회원 삭제 (DB에서 완전히 제거 or active 플래그 사용)
		memberRepository.delete(member); // 완전히 삭제하는 경우

		// member.setActive(false); // soft delete 방식
		// memberRepository.save(member);

		log.info("회원 삭제 완료: {}", memberId);
	}
}
