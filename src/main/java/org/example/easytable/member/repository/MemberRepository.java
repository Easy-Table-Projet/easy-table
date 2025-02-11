package org.example.easytable.member.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.example.easytable.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByEmail(String email);

	// 이메일 또는 사용자명에 포함된 값을 기준으로 검색 (부분 일치)
	Page<Member> findByEmailContainingOrNameContaining(String email, String name, Pageable pageable);

}