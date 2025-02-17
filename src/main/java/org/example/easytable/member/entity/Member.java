package org.example.easytable.member.entity;

import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.easytable.common.entity.BaseEntity;

@Getter
@NoArgsConstructor
@Entity
@Table
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Email
	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MemberType memberType = MemberType.NONE;

	@Column(nullable = false)
	private boolean isDeleted = false;

	@Builder
	public Member(String name, String email, String password, MemberType memberType) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.memberType = memberType;
	}

	public void updatePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public void softDelete() {
		this.isDeleted = true;
	}
}