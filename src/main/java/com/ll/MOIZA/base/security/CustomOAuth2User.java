package com.ll.MOIZA.base.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends User implements OAuth2User, UserDetails {
    private final Long id;
    private final String profile;

    public CustomOAuth2User(String username, Long id, String profile, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);

        this.profile = profile;
        this.id = id;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
