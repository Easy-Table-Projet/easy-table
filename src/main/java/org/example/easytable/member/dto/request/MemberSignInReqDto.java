package org.example.easytable.member.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberSignInReqDto {

	private final String email;
	private final String name;
	private final String password;
}