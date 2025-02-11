package org.example.easytable.member.dto.response;

import lombok.Getter;
import org.example.easytable.member.entity.Member;

@Getter
public class MemberUpdateResDto {
	private final Long id;
	private final String email;
	private final String name;  // 수정된 필드명 유지
	private final String address;
	private final Member.UserType userType;  // UserType 내부 enum 사용

	public MemberUpdateResDto(Member savedMember) {
		this.id = savedMember.getId();
		this.email = savedMember.getEmail();
		this.name = savedMember.getName();  // 수정: getMemberName() → getName()
		this.address = savedMember.getAddress();
		this.userType = savedMember.getUserType();
	}
}