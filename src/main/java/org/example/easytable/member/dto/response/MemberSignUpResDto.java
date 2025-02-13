package org.example.easytable.member.dto.response;

import java.time.LocalDateTime;

import org.example.easytable.member.entity.Member;
import lombok.Getter;

@Getter
public class MemberSignUpResDto {

	private Long memberId;
	private String memberName;
	private String email;
	private String address;
	private String userType;
	private LocalDateTime createdAt;

	public MemberSignUpResDto(Member member) {
		this.memberId = member.getId();
		this.memberName = member.getMemberName();
		this.email = member.getEmail();
		this.address = member.getAddress();
		this.userType = member.getUserType().name();
		this.createdAt = member.getCreatedAt();
	}
}