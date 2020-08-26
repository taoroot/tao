package com.github.taoroot.tao.system.service.impl;

import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.security.SecurityUtils;
import com.github.taoroot.tao.system.datascope.DataScope;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.entity.SysUserRole;
import com.github.taoroot.tao.system.mapper.SysUserMapper;
import com.github.taoroot.tao.system.mapper.SysUserRoleMapper;
import com.github.taoroot.tao.system.service.SysUserRoleService;
import com.github.taoroot.tao.system.service.SysUserService;
import com.github.taoroot.tao.utils.R;
import com.github.taoroot.tao.utils.TreeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleService sysUserRoleService;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public R userInfo() {
        Integer userId = SecurityUtils.userId();
        HashMap<String, Object> result = new HashMap<>();
        // 查询用户个人信息
        result.put("info", sysUserMapper.selectById(SecurityUtils.userId()));
        // 查询用户角色信息
        result.put("roles", sysUserMapper.roles(userId));
        // 功能: 1
        result.put("functions", sysUserMapper.authorities(userId, 1));
        // 菜单: 0
        List<SysAuthority> menus = sysUserMapper.authorities(userId, 0);
        result.put("menus", TreeUtil.build(menus, TreeUtils.ROOT_PARENT_ID, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setWeight(treeNode.getWeight());
            tree.setName(treeNode.getName());
            tree.putExtra("path", treeNode.getPath());
            tree.putExtra("hidden", treeNode.getHidden());
            tree.putExtra("alwaysShow", treeNode.getAlwaysShow());
            tree.putExtra("redirect", treeNode.getRedirect());
            tree.putExtra("type", treeNode.getType());
            tree.put("component", treeNode.getComponent());
            HashMap<String, Object> meta = new HashMap<>();
            meta.put("title", treeNode.getTitle());
            meta.put("icon", treeNode.getIcon());
            meta.put("breadcrumb", treeNode.getBreadcrumb());
            tree.putExtra("meta", meta);
        }));
        return R.ok(result);
    }

    @Override
    public R getPage(Page<SysUser> page, String username, String phone, Integer deptId, Boolean enabled) {
        IPage<SysUser> result = sysUserMapper.getPage(page, new DataScope(), username, phone, deptId, enabled);
        return R.ok(result);
    }

    @Override
    public R saveOrUpdateItem(SysUser sysUser) {
        // 更新用户信息
        sysUser.updateById();

        // 更新角色信息
        if (sysUser.getRoles() != null) {
            List<SysUserRole> roleMenuList = Arrays.stream(sysUser.getRoles()).map(userId -> {
                SysUserRole userRole = new SysUserRole();
                userRole.setRoleId(userId);
                userRole.setUserId(sysUser.getId());
                return userRole;
            }).collect(Collectors.toList());
            sysUserRoleMapper.delete(Wrappers.<SysUserRole>query().lambda()
                    .eq(SysUserRole::getUserId, sysUser.getId()));
            sysUserRoleService.saveBatch(roleMenuList);
        }

        return R.ok();
    }
}
