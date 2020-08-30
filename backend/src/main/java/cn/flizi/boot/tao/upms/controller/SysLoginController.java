package cn.flizi.boot.tao.upms.controller;

import cn.flizi.boot.tao.security.SecurityUtils;
import cn.flizi.boot.tao.upms.entity.SysUser;
import cn.flizi.boot.tao.upms.entity.SysUserOauth2;
import cn.flizi.boot.tao.upms.mapper.SysUserOauth2Mapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.flizi.boot.tao.upms.service.SysUserService;
import cn.flizi.boot.tao.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/user_info")
    public R userInfo(@RequestBody SysUser sysUser) {
        SysUser sysUser1 = new SysUser();
        sysUser1.setId(sysUser.getId());
        sysUser1.setUsername(sysUser.getUsername());
        sysUser1.setNickname(sysUser.getNickname());
        sysUser1.setEmail(sysUser.getEmail());
        sysUser1.setPhone(sysUser.getPhone());
        if (sysUser.getId().equals(SecurityUtils.userId())) {
            if (sysUser.updateById()) {
                return R.okMsg("修改成功");
            }
        }
        return R.errMsg("参数错误");
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

