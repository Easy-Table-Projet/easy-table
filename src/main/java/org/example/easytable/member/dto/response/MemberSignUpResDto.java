package org.example.easytable.member.dto.response;

import org.example.easytable.member.entity.Member; // Member 엔티티를 가져옵니다
import org.example.easytable.member.entity.Member.UserType;

import lombok.Getter;

@Getter
public class MemberSignUpResDto {
	private Long id;
	private final String email;
	private final String name;
	private final String address;  // 추가
	private final UserType userType;  // 추가

	public MemberSignUpResDto(Member member) {
		this.id = member.getId();
		this.email = member.getEmail();
		this.name = member.getName();
		this.address = member.getAddress();  // 추가
		this.userType = member.getUserType();  // Member 객체에서 UserType을 가져옵니다
	}
}