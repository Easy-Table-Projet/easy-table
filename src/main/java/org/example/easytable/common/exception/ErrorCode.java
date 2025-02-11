package org.example.easytable.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다."),
	EMAIL_EXISTS("이미 존재하는 이메일입니다."),
	USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
	INVALID_PASSWORD("잘못된 비밀번호입니다."),
	ID_MISMATCH("ID가 일치하지 않습니다."),
	ALREADY_DELETED_USER("사용자가 탈퇴했습니다.");
	private final String message;
}
