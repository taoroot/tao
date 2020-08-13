package com.github.taoroot.tao.security.auth.oauth2;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class QQOAuth2User implements OAuth2User {
    public static final String TYPE = "qq";
    public static final String APP_ID = "appid";
    public static final String SECRET = "secret";

    private final List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
    private Map<String, Object> attributes;
    private String openid;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
            this.attributes.put("openid", this.getOpenid());
        }
        return attributes;
    }


    @Override
    public String getName() {
        return this.openid;
    }

}
