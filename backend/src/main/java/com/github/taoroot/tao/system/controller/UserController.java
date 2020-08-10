package com.github.taoroot.tao.system.controller;

import com.github.taoroot.tao.utils.R;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class UserController {

    @SneakyThrows
    @GetMapping("/user/info")
    public R index() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", authentication.getName());
        hashMap.put("avatar", "http://cdn.flizi.cn/img/zhiyi-avatar.jpg");
        hashMap.put("other", authentication);
        return R.ok(hashMap);
    }

}

