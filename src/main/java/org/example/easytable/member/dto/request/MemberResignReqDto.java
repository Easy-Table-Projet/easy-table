package org.example.easytable.member.dto.request;

import lombok.Getter;

@Getter
public class MemberResignReqDto {

	private final String email;

	private final String name;

	private final String password;

	public MemberResignReqDto(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
	}
}