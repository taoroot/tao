package com.github.taoroot.tao.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.security.CustomUserDetails;
import com.github.taoroot.tao.security.CustomUserDetailsService;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.entity.SysUserOauth2;
import com.github.taoroot.tao.system.mapper.SysUserMapper;
import com.github.taoroot.tao.system.mapper.SysUserOauth2Mapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author zhiyi
 */
@Log4j2
@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService, CustomUserDetailsService {

    private final SysUserOauth2Mapper sysUserOauth2Mapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomUserDetails createUser(CustomUserDetails user) {
        return null;
    }

    @Override
    public void updateUser(CustomUserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }


    @Override
    public CustomUserDetails loadUserByOAuth(String clientId, String principalName, boolean create) {

        SysUserOauth2 userOauth2 = sysUserOauth2Mapper.selectOne(Wrappers.<SysUserOauth2>lambdaQuery()
                .eq(SysUserOauth2::getClientRegistrationId, clientId)
                .eq(SysUserOauth2::getPrincipalName, principalName)
        );

        if (userOauth2 != null) {
            return translate(getById(userOauth2.getUserId()));
        }

        if (create) {
            String username = UUID.randomUUID().toString().replaceAll("-", "");
            String password = UUID.randomUUID().toString().replaceAll("-", "");

            SysUser sysUser = new SysUser();
            sysUser.setUsername(username);
            sysUser.setPassword(passwordEncoder.encode(password));
            sysUser.insert();

            bindOauth2(clientId, principalName, sysUser.getId());

            return translate(sysUser);
        }
        return null;
    }

    @Override
    public String bindOauth2(String clientId, String principalName, Integer userId) {
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
        userOauth2.setPrincipalName(principalName);
        userOauth2.insert();
        return "绑定成功 " + userOauth2.getClientRegistrationId() + " : " + userOauth2.getPrincipalName();
    }

    @Override
    public CustomUserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        SysUser myUser = getBaseMapper().selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPhone, phone));

        if (myUser == null) {
            throw new UsernameNotFoundException("手机号不存在");
        }

        return translate(myUser);
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser myUser = getBaseMapper().selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));

        if (myUser == null) {
            throw new UsernameNotFoundException(username);
        }

        return translate(myUser);
    }

    @Override
    public CustomUserDetails loadUserById(String userId) {
        return translate(getBaseMapper().selectById(userId));
    }

    private CustomUserDetails translate(SysUser myUser) {
        assert myUser != null;
        return new CustomUserDetails(
                myUser.getUsername(),
                myUser.getPassword(),
                myUser.getPhone(),
                myUser.getId(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(myUser.getRoles()));
    }
}
