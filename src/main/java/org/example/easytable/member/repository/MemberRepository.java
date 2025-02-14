package org.example.easytable.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.easytable.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}