package com.github.taoroot.tao.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.taoroot.tao.security.annotation.NotAuth;
import com.github.taoroot.tao.system.entity.SysUserOauth2;
import com.github.taoroot.tao.system.mapper.SysUserOauth2Mapper;
import com.github.taoroot.tao.system.service.ISysUserService;
import com.github.taoroot.tao.utils.R;
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

    @Resource
    private ISysUserService iSysUserService;

    @GetMapping("/user/info")
    public R userInfo() {
        return R.ok(iSysUserService.userInfo());
    }

    @GetMapping("/user/socials")
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

    @NotAuth
    @GetMapping("/user/authorities")
    public R userAuthorities() {
        return R.ok(iSysUserService.userMenus());
    }
}

