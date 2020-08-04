package com.github.taoroot.tao.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomUserDetailsService extends UserDetailsService {

    /**
     * 通过手机号登录
     */
    UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException;
}
