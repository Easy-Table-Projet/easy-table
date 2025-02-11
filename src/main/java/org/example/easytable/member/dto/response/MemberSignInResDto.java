package org.example.easytable.member.dto.response;

import lombok.Getter;

@Getter
public class MemberSignInResDto {
	private final String token;

	public MemberSignInResDto(String token) {
		this.token = token;
	}
}