package org.example.easytable.member.dto.request;

import lombok.Getter;

@Getter
public class MemberSignInReqDto {

	private final String email;
	private final String name;
	private final String password;

	public MemberSignInReqDto(String email, String membername, String password) {
		this.email = email;
		this.name = membername;
		this.password = password;
	}
}