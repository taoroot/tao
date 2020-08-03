package com.github.taoroot.tao.system.service;

import com.github.taoroot.tao.security.CustomUser;
import com.github.taoroot.tao.security.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailServiceImpl implements CustomUserDetailsService {

    CustomUser customUser;

    {
        UserDetails userDetails = CustomUser.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        customUser = new CustomUser(userDetails.getUsername(),
                userDetails.getPassword(),
                "1234567890",
                userDetails.getAuthorities());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!username.equals(customUser.getUsername())) {
            throw new UsernameNotFoundException("手机号不存在");
        }
        return customUser;
    }

    @Override
    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        if (!phone.equals(customUser.getPhone())) {
            throw new UsernameNotFoundException("手机号不存在");
        }
        return customUser;
    }
}
