package org.example.easytable.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MemberUpdateReqDto {
	private final String email;
	private final String membername;
	private String address;
}