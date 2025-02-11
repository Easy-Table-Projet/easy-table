package org.example.easytable.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.easytable.common.entity.BaseEntity;
import org.example.easytable.reservation.entity.Reservation;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String password;

	private String address;

	@Enumerated(EnumType.STRING)
	private UserType userType;

	private boolean isDeleted;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Reservation> reservations;

	public void memberUpdate(String name, String address, UserType userType) {
		this.name = name;
		this.address = address;
		this.userType = userType;
	}

	// 탈퇴 처리 메소드
	public void inActivate() {
		this.isDeleted = true;  // 탈퇴 상태로 변경
	}

	public enum UserType {
		OWNER, USER
	}
}