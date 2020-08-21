package com.github.taoroot.tao.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.security.SecurityUtils;
import com.github.taoroot.tao.system.dto.SysUserPageVO;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.mapper.SysUserMapper;
import com.github.taoroot.tao.system.service.SysUserService;
import com.github.taoroot.tao.utils.R;
import com.github.taoroot.tao.utils.TreeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public R userInfo() {
        Integer userId = SecurityUtils.userId();
        HashMap<String, Object> result = new HashMap<>();
        // 查询用户个人信息
        result.put("info", sysUserMapper.selectById(SecurityUtils.userId()));
        // 查询用户角色信息
        result.put("roles", sysUserMapper.roles(userId));
        // 菜单: 0
        List<SysAuthority> menus = sysUserMapper.authorities(userId, 0);
        result.put("menus", TreeUtils.authorityTree(menus));
        // 功能: 1
        result.put("functions", sysUserMapper.authorities(userId, 1));
        return R.ok(result);
    }

    @Override
    public R getPage(Page<SysUser> page) {
        IPage<SysUserPageVO> result = sysUserMapper.getPage(page);
        return R.ok(result);
    }
}
