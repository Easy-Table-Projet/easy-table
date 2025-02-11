package org.example.easytable.member.dto.request;

import lombok.Getter;

@Getter
public class MemberSignUpReqDto {

	private final String email;
	private final String membername;
	private final String password;
	private final String address;
	private final String userType;

	public MemberSignUpReqDto(String email, String membername, String password, String address, String userType) {
		this.email = email;
		this.membername = membername;
		this.password = password;
		this.address = address;
		this.userType = userType;
	}
}