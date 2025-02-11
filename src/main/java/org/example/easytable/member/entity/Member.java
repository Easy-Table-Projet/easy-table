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
	@Column(
		name = "member_name",
		nullable = false
	)
	private String memberName;

	@Comment("이메일")
	@Column(
		name = "email",
		nullable = false
	)
	private String email;

	@Comment("비밀번호")
	@Column(
		name = "password",
		nullable = false
	)
	private String password;

	@Comment("주소")
	@Column(
		name = "address",
		nullable = false
	)
	private String address;

	@Comment("유저 유형")
	@Column(
		name = "user_type",
		nullable = false
	)
	private String userType; // OWNER/USER

	@Comment("유저 활성화")
	@Column(
		name = "active",
		nullable = false
	)
	private boolean active; // 선언만

	protected Member() {}

	// 이메일, 이름, 비밀번호를 받는 생성자 추가
	public Member(String email, String memberName, String password) {
		this.email = email;
		this.memberName = memberName;
		this.password = password;
		this.active = true; // 기본값으로 활성화 상태를 true로 설정
	}

	public void inActivate() {
		this.active = false;
	}

	public void updateMember(MemberUpdateReqDto updateUserRequestDto) {
		this.memberName = updateUserRequestDto.getMembername();
		this.address = updateUserRequestDto.getAddress();
	}
}