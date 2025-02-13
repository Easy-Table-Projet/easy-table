package org.example.easytable.member.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.easytable.member.entity.Member;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class MemberUpdateResDto {
	private final Long memberId;
	private final String membername;
	private final String email;
	private final String address;
	private final String userType;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	public MemberUpdateResDto(Member savedMember) {
		this.memberId = savedMember.getId();
		this.membername = savedMember.getMemberName();
		this.email = savedMember.getEmail();
		this.address = savedMember.getAddress();
		this.userType = savedMember.getUserType().name();
		this.createdAt = savedMember.getCreatedAt();
		this.updatedAt = savedMember.getUpdatedAt();
	}
}