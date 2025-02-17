package org.example.easytable.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RegisterReqDto {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private final String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private final String password;

    @NotNull(message = "회원 유형을 선택해주세요")
    private final String memberType;
}