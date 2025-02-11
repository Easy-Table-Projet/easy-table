package org.example.easytable.member.service;

import org.example.easytable.member.dto.response.MemberGetResDto;
import org.example.easytable.member.entity.Member;
import org.example.easytable.common.exception.CustomException;
import org.example.easytable.common.exception.ErrorCode;
import org.example.easytable.member.dto.request.MemberUpdateReqDto;
import org.example.easytable.member.dto.response.MemberUpdateResDto;
import org.example.easytable.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	// 유저 단건 조회
	public MemberGetResDto getMemberById(Long memberId, Long signInMemberId) {
		// 멤버 아이디로 사용자 존재 여부 확인
		Member member = findMemberById(memberId);

		// 인증된 유저가 조회하려는 회원의 정보에 접근할 수 있는 권한이 있는지 확인
		if (!member.getId().equals(signInMemberId)) {
			throw new CustomException(ErrorCode.ID_MISMATCH); // 본인만 조회할 수 있도록 예외 처리
		}

		// 회원 정보 응답 DTO로 변환하여 반환
		return new MemberGetResDto(member);
	}

	// 멤버 아이디로 사용자 조회
	private Member findMemberById(Long memberId) {
		// 멤버 아이디로 사용자 존재 여부 확인
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); // 회원이 없으면 예외 발생
	}

	// 유저 수정
	public MemberUpdateResDto updateMember(Long memberId, MemberUpdateReqDto memberUpdateReqDto, Long signInMemberId) {
		Member member = findMemberById(memberId);

		if (!member.getId().equals(signInMemberId)) {
			throw new CustomException(ErrorCode.ID_MISMATCH); // 사용자 본인만 접근 가능하도록 예외 처리
		}

		member.updateMember(memberUpdateReqDto);
		Member savedMember = memberRepository.save(member);

		return new MemberUpdateResDto(savedMember);
	}
}