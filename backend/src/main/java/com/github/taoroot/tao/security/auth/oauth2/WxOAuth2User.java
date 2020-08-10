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
public class WxOAuth2User implements OAuth2User {
    private final List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
    private Map<String, Object> attributes;
    private String openid;
    private String nickname;
    private Integer sex;
    private String name;
    private String language;
    private String city;
    private String province;
    private String headimgurl;
    private String[] privilege;
    private String unionid;
    private String errmsg;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
            this.attributes.put("openid", this.getOpenid());
            this.attributes.put("unionid", this.getUnionid());
        }
        return attributes;
    }


    @Override
    public String getName() {
        return this.openid;
    }

}
