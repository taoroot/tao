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
public class GiteeOAuth2User implements OAuth2User {

    public static final String TYPE = "gitee";

    private final List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
    private Map<String, Object> attributes;
    private String id;
    private String name;
    private String login;
    private String email;
    private String avatarUrl;
    private String bio;
    private String blog;
    private String created_at;
    private String events_url;
    private String followers;
    private String followers_url;
    private String following;
    private String following_url;
    private String gists_url;
    private String html_url;
    private String organizations_url;
    private String public_gists;
    private String public_repos;
    private String received_events_url;
    private String type;
    private String updated_at;
    private String url;
    private String watched;
    private String weibo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
//            this.attributes.put("id", this.getId());
//            this.attributes.put("name", this.getName());
//            this.attributes.put("login", this.getLogin());
//            this.attributes.put("email", this.getEmail());
        }
        return attributes;
    }

    @Override
    public String getName() {
        return this.login;
    }
}
