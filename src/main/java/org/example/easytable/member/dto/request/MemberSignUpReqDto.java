package org.example.easytable.member.dto.request;

import org.example.easytable.member.entity.Member;
import lombok.Getter;

@Getter
public class MemberSignUpReqDto {

	private final String email;
	private final String name;
	private final String password;
	private final String address;
	private final Member.UserType userType;

	public MemberSignUpReqDto(String email, String name, String password, String address, Member.UserType userType) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.address = address;
		this.userType = userType;
	}
}