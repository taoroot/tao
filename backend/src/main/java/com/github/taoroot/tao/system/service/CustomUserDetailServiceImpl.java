package com.github.taoroot.tao.system.service;

import com.github.taoroot.tao.security.CustomUser;
import com.github.taoroot.tao.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CustomUserDetailServiceImpl implements CustomUserDetailsService {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!username.equals("username")) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new CustomUser(
                "username",
                new BCryptPasswordEncoder().encode("password"),
                "1234567890", Collections.emptyList());
    }

    @Override
    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        if (!phone.equals("1234567890")) {
            throw new UsernameNotFoundException("手机号不存在");
        }

        return new CustomUser(
                "username",
                new BCryptPasswordEncoder().encode("password"),
                "1234567890", Collections.emptyList());
    }


}
