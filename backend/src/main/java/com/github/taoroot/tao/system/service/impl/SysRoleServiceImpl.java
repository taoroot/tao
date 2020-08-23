package com.github.taoroot.tao.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.dto.SysRoleVo;
import com.github.taoroot.tao.system.entity.SysRole;
import com.github.taoroot.tao.system.entity.SysRoleAuthority;
import com.github.taoroot.tao.system.mapper.SysRoleAuthorityMapper;
import com.github.taoroot.tao.system.mapper.SysRoleMapper;
import com.github.taoroot.tao.system.service.SysRoleAuthorityService;
import com.github.taoroot.tao.system.service.SysRoleService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleAuthorityService sysRoleAuthorityService;

    @Override
    public R getPage(Page<SysRole> page) {
        return R.ok(sysRoleMapper.getPage(page));
    }

    @Override
    public R updateItem(SysRoleVo sysRoleVo) {
        SysRole sysRole = new SysRole();
        sysRole.setId(sysRoleVo.getId());
        sysRole.setDescription(sysRoleVo.getDescription());
        sysRole.setScope(sysRoleVo.getScope());
        sysRole.setRole(sysRoleVo.getRole());
        sysRoleMapper.updateById(sysRole);

        List<SysRoleAuthority> roleMenuList =
                Arrays.stream(sysRoleVo.getAuthorities()).sequential().map(menuId -> {
                    SysRoleAuthority roleMenu = new SysRoleAuthority();
                    roleMenu.setRoleId(sysRoleVo.getId());
                    roleMenu.setAuthorityId(menuId);
                    return roleMenu;
                }).collect(Collectors.toList());
        sysRoleAuthorityService.remove(Wrappers.<SysRoleAuthority>query().lambda()
                .eq(SysRoleAuthority::getRoleId, sysRoleVo.getId()));
        sysRoleAuthorityService.saveBatch(roleMenuList);
        return R.ok();
    }
}
