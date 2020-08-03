package com.github.taoroot.tao.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUser extends User {

    private String phone;

    public CustomUser(String username, String password, String phone, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.phone = phone;
    }

    public CustomUser(String username, String password, String phone, boolean enabled, boolean accountNonExpired,
                      boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }
}
