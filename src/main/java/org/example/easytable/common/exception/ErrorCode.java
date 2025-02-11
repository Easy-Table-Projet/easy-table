package org.example.easytable.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다.");

	private final String message;
}
