package org.example.easytable.member.dto.response;

import org.example.easytable.member.entity.Member;
import lombok.Getter;
import java.time.format.DateTimeFormatter;

@Getter
public class MemberGetResDto {

	private Long memberId;
	private String name;
	private String email;
	private String address;
	private String userType;
	private String createdAt;
	private String updatedAt;

	public MemberGetResDto(Member member) {
		this.memberId = member.getId();
		this.name = member.getMemberName();
		this.email = member.getEmail();
		this.address = member.getAddress();
		this.userType = member.getUserType();
		this.createdAt = member.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.updatedAt = member.getUpdatedAt() != null
			? member.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
			: null; // 업데이트 시간 처리
	}
}