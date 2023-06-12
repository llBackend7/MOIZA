package com.ll.MOIZA.base.security;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    // 카카오톡 로그인이 성공할 때 마다 이 함수가 실행된다.
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String nickname = (String)((Map)oAuth2User.getAttribute("properties")).get("nickname");
        String profile_image_url =(String)((Map)oAuth2User.getAttribute("properties")).get("profile_image");
        String email = (String)((Map)oAuth2User.getAttribute("kakao_account")).get("email");

        Member member = memberService.whenSocialLogin(nickname, profile_image_url, email);

        return new CustomOAuth2User(member.getName(), member.getId(), profile_image_url,"", member.getGrantedAuthorities());
    }
}

