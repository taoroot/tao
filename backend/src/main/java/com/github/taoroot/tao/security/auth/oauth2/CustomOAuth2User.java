package com.github.taoroot.tao.security.auth.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface CustomOAuth2User extends OAuth2User {

    default String getNickname() {
        return getName();
    }

    default String getAvatar() {
        return null;
    }

    @Override
    default Map<String, Object> getAttributes() {
        return new HashMap<>();
    }

    @Override
   default Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }
}
