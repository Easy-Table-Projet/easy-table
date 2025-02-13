package org.example.easytable.member.dto.request;

import lombok.Getter;

@Getter
public class MemberSignUpReqDto {

	private String email;
	private String membername;
	private String password;
	private String address;
	private String userType;

	public MemberSignUpReqDto(String email, String membername, String password, String address, String userType) {
		this.email = email;
		this.membername = membername;
		this.password = password;
		this.address = address;
		this.userType = userType;
	}
}