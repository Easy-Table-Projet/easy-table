package org.example.easytable.member.dto.response;

import lombok.Getter;

@Getter
public class MemberSignInResDto {
	private String token;

	public MemberSignInResDto(String token) {
		this.token = token;
	}
}