package org.example.easytable.common.utills;

import org.example.easytable.common.exception.CustomException;
import org.example.easytable.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import at.favre.lib.crypto.bcrypt.BCrypt;

@Component
public class PasswordEncoder {

	// Bcrypt : 단방향 해시 알고리즘

	// 일반 문자열을 암호화
	public String encode(String rawPassword) {
		return BCrypt.withDefaults().hashToString(BCrypt.MIN_COST, rawPassword.toCharArray());
	}

	public boolean matches(String rawPassword, String encodedPassword) {
		// 입력한 비밀번호(rawPassword)와 저장된 암호화된 비밀번호(encodedPassword) 비교
		BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);

		// 비밀번호가 일치하지 않으면 false 반환
		if (!result.verified) {
			throw new CustomException(ErrorCode.PASSWORD_MISMATCH);  // 비밀번호 불일치 시 예외 처리
		}
		// 비밀번호가 일치하면 true 반환
		return true;
	}

}
