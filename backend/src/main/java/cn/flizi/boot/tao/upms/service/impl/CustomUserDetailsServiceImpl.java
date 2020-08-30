package cn.flizi.boot.tao.upms.service.impl;

import cn.flizi.boot.tao.security.CustomUserDetailsService;
import cn.flizi.boot.tao.upms.entity.SysAuthority;
import cn.flizi.boot.tao.upms.entity.SysUser;
import cn.flizi.boot.tao.upms.entity.SysUserOauth2;
import cn.flizi.boot.tao.upms.mapper.SysUserOauth2Mapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.flizi.boot.tao.security.CustomUserDetails;
import cn.flizi.boot.tao.security.auth.oauth2.CustomOAuth2User;
import cn.flizi.boot.tao.upms.entity.SysRole;
import cn.flizi.boot.tao.upms.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public CustomUserDetails loadUserByOAuth2(String clientId, CustomOAuth2User oAuth2User) {

        SysUserOauth2 userOauth2 = sysUserOauth2Mapper.selectOne(Wrappers.<SysUserOauth2>lambdaQuery()
                .eq(SysUserOauth2::getClientRegistrationId, clientId)
                .eq(SysUserOauth2::getPrincipalName, oAuth2User.getName())
        );

        if (userOauth2 != null) {
            return translate(sysUserMapper.selectById(userOauth2.getUserId()));
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
        List<SysAuthority> authorities = sysUserMapper.authorities(myUser.getId(), 1);
        List<SysRole> roles = sysUserMapper.roles(myUser.getId());
        List<String> collect = authorities.stream().map(SysAuthority::getAuthority).collect(Collectors.toList());
        List<String> collect1 = roles.stream().map(SysRole::getRole).collect(Collectors.toList());
        collect.addAll(collect1);

        return new CustomUserDetails(
                myUser.getUsername(),
                myUser.getPassword(),
                myUser.getPhone(),
                myUser.getId(),
                myUser.getEnabled(),
                AuthorityUtils.createAuthorityList(collect.toArray(new String[0]))
        );
    }
}
