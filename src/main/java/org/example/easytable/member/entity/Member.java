package org.example.easytable.member.entity;

import org.example.easytable.common.entity.BaseEntity;
import org.example.easytable.member.dto.request.MemberUpdateReqDto;
import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_name", nullable = false)
	private String memberName;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "user_type", nullable = false)
	@Enumerated(EnumType.STRING)  // Enum 값을 문자열로 저장
	private UserType userType; // OWNER/USER

	@Column(name = "active", nullable = false)
	private boolean active = true; // 기본값을 선언부에서 설정

	// 이메일, 이름, 비밀번호를 받는 생성자
	public Member(String email, String memberName, String password, String address, UserType userType) {
		this.email = email;
		this.memberName = memberName;
		this.password = password;
		this.address = address;
		this.userType = userType;
	}

	// 유저 비활성화 (탈퇴 처리)
	public void inactivate() {
		this.active = false;
	}

	// 유저 정보 업데이트
	public void updateMember(MemberUpdateReqDto updateUserRequestDto) {
		this.memberName = updateUserRequestDto.getMembername();
		this.address = updateUserRequestDto.getAddress();
	}
}