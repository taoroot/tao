package com.github.taoroot.tao.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.taoroot.tao.security.SecurityUtils;
import com.github.taoroot.tao.system.entity.SysUserOauth2;
import com.github.taoroot.tao.system.mapper.SysUserOauth2Mapper;
import com.github.taoroot.tao.system.service.SysUserService;
import com.github.taoroot.tao.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@RestController
public class SysLoginController {
    @Autowired
    OAuth2ClientProperties properties;

    @Resource
    private SysUserOauth2Mapper sysUserOauth2Mapper;

    @Resource
    private SysUserService sysUserService;

    @GetMapping("/user_info")
    public R userInfo() {
        return sysUserService.userInfo();
    }

    @GetMapping("/user_socials")
    public R social() {
        Collection<ClientRegistration> clientRegistrations = OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties).values();
        HashMap<String, SysUserOauth2> result = new HashMap<>();

        // 全部
        for (ClientRegistration clientRegistration : clientRegistrations) {
            SysUserOauth2 userOauth2 = new SysUserOauth2();
            userOauth2.setClientRegistrationId(clientRegistration.getRegistrationId());
            result.put(clientRegistration.getRegistrationId(), userOauth2);
        }
        // 覆盖已绑定的
        List<SysUserOauth2> list = sysUserOauth2Mapper.selectList(
                Wrappers.<SysUserOauth2>lambdaQuery().eq(SysUserOauth2::getUserId, SecurityUtils.userId())
        );
        for (SysUserOauth2 userOauth2 : list) {
            result.put(userOauth2.getClientRegistrationId(), userOauth2);
        }

        return R.ok(result.values());
    }


    @DeleteMapping("/user_social/{id}")
    public R social(@PathVariable String id) {
        SysUserOauth2 userOauth2 = sysUserOauth2Mapper.selectById(id);
        if (userOauth2 == null) {
            return R.errMsg("账号不存在");
        }

        if (!userOauth2.getUserId().equals(SecurityUtils.userId())) {
            return R.errMsg("参数错误");
        }

        sysUserOauth2Mapper.deleteById(userOauth2.getId());

        return R.okMsg("解绑成功");
    }
}

