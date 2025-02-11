package org.example.easytable.member.dto.request;

import lombok.Getter;
import org.example.easytable.member.entity.Member.UserType;

@Getter
public class MemberUpdateReqDto {
	private String name;
	private String address;
	private UserType userType;
}