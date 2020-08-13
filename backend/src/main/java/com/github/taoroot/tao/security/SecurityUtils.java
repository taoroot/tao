package com.github.taoroot.tao.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Integer userId() {
        return Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());
    }

}
