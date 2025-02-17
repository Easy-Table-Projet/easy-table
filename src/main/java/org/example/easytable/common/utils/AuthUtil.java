package org.example.easytable.common.utils;

import lombok.experimental.UtilityClass;
import org.example.easytable.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@UtilityClass // 유틸리티 클래스로 선언
public class AuthUtil {


    public Long getId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        return null; // 인증되지 않은 경우
    }


    public List<String> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        }

        return List.of(); // 인증되지 않은 경우 빈 리스트 반환
    }
}
