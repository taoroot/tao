package com.github.taoroot.tao.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.taoroot.tao.security.CustomUserDetails;
import com.github.taoroot.tao.security.CustomUserDetailsService;
import com.github.taoroot.tao.security.SecurityUtils;
import com.github.taoroot.tao.security.auth.oauth2.CustomOAuth2User;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.entity.SysUserOauth2;
import com.github.taoroot.tao.system.entity.SysUserRole;
import com.github.taoroot.tao.system.mapper.SysUserMapper;
import com.github.taoroot.tao.system.mapper.SysUserOauth2Mapper;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author zhiyi
 */
@Log4j2
@Service
@AllArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final SysUserOauth2Mapper sysUserOauth2Mapper;

    private final PasswordEncoder passwordEncoder;

    private final SysUserMapper sysUserMapper;


    @Override
    public CustomUserDetails loadUserByOAuth2(String clientId, CustomOAuth2User oAuth2User, boolean create) {

        SysUserOauth2 userOauth2 = sysUserOauth2Mapper.selectOne(Wrappers.<SysUserOauth2>lambdaQuery()
                .eq(SysUserOauth2::getClientRegistrationId, clientId)
                .eq(SysUserOauth2::getPrincipalName, oAuth2User.getName())
        );

        if (userOauth2 != null) {
            return translate(sysUserMapper.selectById(userOauth2.getUserId()));
        }

        if (create) {
            String username = clientId + oAuth2User.getName();
            String password = UUID.randomUUID().toString().replaceAll("-", "");
            SysUser sysUser = new SysUser();
            sysUser.setUsername(username);
            sysUser.setAvatar(oAuth2User.getAvatar());
            sysUser.setPassword(passwordEncoder.encode(password));
            sysUser.setDeptId(1000);
            sysUser.insert();

            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(sysUser.getId());
            sysUserRole.setRoleId(1);
            sysUserRole.insert();

            bindOauth2(clientId, oAuth2User, sysUser.getId());

            return translate(sysUser);
        }
        return null;
    }

    @Override
    public String bindOauth2(String clientId, CustomOAuth2User oAuth2User, Integer userId) {
        // 查询是否有相同类型
        int count = sysUserOauth2Mapper.selectCount(Wrappers.<SysUserOauth2>lambdaQuery()
                .eq(SysUserOauth2::getClientRegistrationId, clientId)
                .eq(SysUserOauth2::getUserId, userId));

        if (count > 0) {
            return "已有绑定,请先解绑";
        }

        SysUserOauth2 userOauth2 = new SysUserOauth2();
        userOauth2.setUserId(userId);
        userOauth2.setClientRegistrationId(clientId);
        userOauth2.setPrincipalName(oAuth2User.getName());
        userOauth2.setNickname(oAuth2User.getNickname());
        userOauth2.setAvatar(oAuth2User.getAvatar());
        userOauth2.insert();
        return "绑定成功 " + userOauth2.getClientRegistrationId() + " : " + userOauth2.getPrincipalName();
    }

    @Override
    public CustomUserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        SysUser myUser = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPhone, phone));

        if (myUser == null) {
            throw new UsernameNotFoundException("手机号不存在");
        }

        return translate(myUser);
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser myUser = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));

        if (myUser == null) {
            throw new UsernameNotFoundException(username);
        }

        return translate(myUser);
    }

    @Override
    public CustomUserDetails loadUserById(String userId) {
        return translate(sysUserMapper.selectById(userId));
    }

    private CustomUserDetails translate(SysUser myUser) {
        assert myUser != null;
        return new CustomUserDetails(
                myUser.getUsername(),
                myUser.getPassword(),
                myUser.getPhone(),
                myUser.getId(),
                org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
    }


}
