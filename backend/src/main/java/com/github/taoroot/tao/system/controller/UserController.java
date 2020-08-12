package com.github.taoroot.tao.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.taoroot.tao.security.CustomUserDetailsService;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.entity.SysUserOauth2;
import com.github.taoroot.tao.system.mapper.SysUserOauth2Mapper;
import com.github.taoroot.tao.system.service.ISysUserService;
import com.github.taoroot.tao.utils.R;
import lombok.SneakyThrows;
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
    private CustomUserDetailsService customUserDetailsService;

    @Resource
    private ISysUserService iSysUserService;

    @SneakyThrows
    @GetMapping("/user/info")
    public R index() {
        SysUser byId = iSysUserService.getById(SecurityContextHolder.getContext().getAuthentication().getName());
        byId.setAvatar("http://cdn.flizi.cn/img/zhiyi-avatar.jpg");
        return R.ok(byId);
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

