package com.ll.MOIZA.base.initData;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test"})
public class NotProd {
    @Bean
    CommandLineRunner commandLineRunner(
            MemberRepository memberRepository
    ) {
        return args -> {
            Member member1 = Member.builder().name("user1").password("1234").email("email").build();
            Member member2 = Member.builder().name("user2").password("1234").email("email").build();
            memberRepository.save(member1);
            memberRepository.save(member2);
        };
    }
}
