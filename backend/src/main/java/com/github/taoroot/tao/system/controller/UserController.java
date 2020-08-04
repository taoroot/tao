package com.github.taoroot.tao.system.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/user/info")
    public Object index() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}

