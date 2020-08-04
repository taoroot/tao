package com.github.taoroot.tao.system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {


    @GetMapping
    public Authentication index() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
