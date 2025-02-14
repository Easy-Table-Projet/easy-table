package org.example.easytable.member.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class MeResDto {
    private final Long id;
    private final List<String> roles;
}
