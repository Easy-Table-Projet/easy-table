package org.example.easytable.member.entity;

import java.util.Arrays;

public enum MemberType {

    NONE, USER, OWNER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    public static boolean isValid(String value) {
        return Arrays.stream(MemberType.values())
                .anyMatch(role -> role.name().equalsIgnoreCase(value));
    }
}
