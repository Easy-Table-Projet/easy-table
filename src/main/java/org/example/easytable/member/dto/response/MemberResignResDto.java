package org.example.easytable.member.dto.response;

import lombok.Getter;

@Getter
public class MemberResignResDto {

	private final String message;

	public MemberResignResDto() {
		this.message = "회원 탈퇴 처리되었습니다.";
	}
}