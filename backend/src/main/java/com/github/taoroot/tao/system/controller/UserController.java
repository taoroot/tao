package com.github.taoroot.tao.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.taoroot.tao.system.entity.SysUserOauth2;
import com.github.taoroot.tao.system.mapper.SysUserOauth2Mapper;
import com.github.taoroot.tao.utils.R;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Resource
    private SysUserOauth2Mapper sysUserOauth2Mapper;

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

    @SneakyThrows
    @GetMapping("/user/social")
    public R social() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<HashMap<String, String>> collect = sysUserOauth2Mapper.selectList(
                Wrappers.<SysUserOauth2>lambdaQuery()
                        .eq(SysUserOauth2::getUserId, name)
        ).stream().map(item -> {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(item.getClientRegistrationId(), item.getPrincipalName());
            return hashMap;
        })
                .collect(Collectors.toList());

        return R.ok(collect);
    }
}

