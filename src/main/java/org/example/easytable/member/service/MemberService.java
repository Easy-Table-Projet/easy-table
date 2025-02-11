package org.example.easytable.member.service;

import org.example.easytable.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.example.easytable.member.entity.Member;
import java.util.Optional;

import org.example.easytable.common.exception.CustomException;
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

	public Member findMemberById(Long memberId) {

		// memberId로 사용자 존재 여부 확인
		Optional<Member> existingMember = memberRepository.findById(memberId);

		// memberId가 존재하지 않으면 예외 처리
		if (existingMember.isEmpty()) {
			log.info("사용자를 찾을 수 없습니다. memberId: {}", memberId);
			throw new CustomException(ErrorCode.USER_NOT_FOUND);  // 사용자를 찾을 수 없으면 예외 처리
		}
		return existingMember.get();
	}

	// 이메일 또는 사용자명으로 부분일치 검색 후 페이징 처리 된 값 반환
	public Page<Member> searchMatchedMembers(String name, String email, Pageable pageable) {
		return memberRepository.findByEmailContainingOrNameContaining(name, email, pageable);
	}

	// 멤버 수정
	public MemberUpdateResDto updateMember(Long memberId, MemberUpdateReqDto memberUpdateReqDto, Long signInMemberId) {
		Member member = findMemberById(memberId);

		if (!member.getId().equals(signInMemberId)) {
			throw new CustomException(ErrorCode.ID_MISMATCH); // 사용자 본인만 접근 가능하도록 예외 처리
		}

		// MemberUpdateReqDto에서 값 추출 후 전달
		member.memberUpdate(memberUpdateReqDto.getName(), memberUpdateReqDto.getAddress(), memberUpdateReqDto.getUserType());

		Member savedMember = memberRepository.save(member);

		return new MemberUpdateResDto(savedMember);
	}
}