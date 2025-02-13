package org.example.easytable.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final ErrorCode errorCode;

	// ErrorCode만 받는 생성자
	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	// ErrorCode와 메시지를 받는 생성자 추가
	public CustomException(ErrorCode errorCode, String message) {
		super(message);  // 사용자 정의 메시지
		this.errorCode = errorCode;
	}

	// 스택 트레이스를 출력할 수 있게 하는 생성자 추가
	public CustomException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);  // 예외 원인과 메시지
		this.errorCode = errorCode;
	}
}
