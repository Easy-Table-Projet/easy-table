package org.example.easytable.member.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;


	public void deleteUser(Integer userId) {
		Member user = memberRepository.findById(userId)
				.orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다. ID: " + userId));

		user.softDelete();
	}
}