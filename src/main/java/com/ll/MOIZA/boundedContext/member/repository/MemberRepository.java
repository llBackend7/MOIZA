package com.ll.MOIZA.boundedContext.member.repository;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
