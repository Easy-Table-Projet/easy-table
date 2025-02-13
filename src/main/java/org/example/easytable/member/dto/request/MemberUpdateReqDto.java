package org.example.easytable.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor // 기본 생성자 추가
@Getter
public class MemberUpdateReqDto {
	private String email;   // optional 처리하거나 null을 허용
	private String membername;
	private String address;
}