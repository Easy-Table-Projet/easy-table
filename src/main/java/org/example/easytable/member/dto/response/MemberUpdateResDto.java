package org.example.easytable.member.dto.response;

import org.example.easytable.member.entity.Member;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberUpdateResDto {
	private final Long id;
	private final String email;
	private final String membername;

	public MemberUpdateResDto(Member savedMember) {
		this.id = savedMember.getId();
		this.email = savedMember.getEmail();
		this.membername = savedMember.getMemberName();
	}
}