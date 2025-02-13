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

@Entity
@Table(name = "MEMBER")
@Getter
public class Member extends BaseEntity {

	@Comment("멤버 식별자")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "BIGINT")
	private Long id;

	@Comment("이름")
	@Column(name = "member_name", nullable = false)
	private String memberName;

	@Comment("이메일")
	@Column(name = "email", nullable = false)
	private String email;

	@Comment("비밀번호")
	@Column(name = "password", nullable = false)
	private String password;

	@Comment("주소")
	@Column(name = "address", nullable = false)
	private String address;

	@Comment("유저 유형")
	@Column(name = "user_type", nullable = false)
	@Enumerated(EnumType.STRING)  // Enum 값을 문자열로 저장
	private UserType userType; // OWNER/USER

	@Comment("유저 활성화")
	@Column(name = "active", nullable = false)
	private boolean active = true; // 기본값을 선언부에서 설정

	// 기본 생성자 (JPA 사용을 위해 필요)
	public Member() {}

	// 이메일, 이름, 비밀번호를 받는 생성자
	public Member(String email, String memberName, String password, String address, UserType userType) {
		this.email = email;
		this.memberName = memberName;
		this.password = password;
		this.address = address;
		this.userType = userType;
	}

	// 유저 비활성화 (탈퇴 처리)
	public void inActivate() {
		this.active = false;
	}

	// 유저 정보 업데이트
	public void updateMember(MemberUpdateReqDto updateUserRequestDto) {
		this.memberName = updateUserRequestDto.getMembername();
		this.address = updateUserRequestDto.getAddress();
	}
}