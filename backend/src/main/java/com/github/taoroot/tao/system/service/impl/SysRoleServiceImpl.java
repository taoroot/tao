package com.github.taoroot.tao.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.security.SecurityUtils;
import com.github.taoroot.tao.system.datascope.DataScopeTypeEnum;
import com.github.taoroot.tao.system.entity.SysRole;
import com.github.taoroot.tao.system.entity.SysRole;
import com.github.taoroot.tao.system.entity.SysRoleAuthority;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.mapper.SysRoleAuthorityMapper;
import com.github.taoroot.tao.system.mapper.SysRoleMapper;
import com.github.taoroot.tao.system.mapper.SysUserMapper;
import com.github.taoroot.tao.system.service.SysDeptService;
import com.github.taoroot.tao.system.service.SysRoleAuthorityService;
import com.github.taoroot.tao.system.service.SysRoleService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleAuthorityService sysRoleAuthorityService;
    private final SysRoleAuthorityMapper sysRoleAuthorityMapper;
    private final SysUserMapper sysUserMapper;
    private final SysDeptService sysDeptService;

    @Override
    public R getPage(Page<SysRole> page) {
        return R.ok(sysRoleMapper.getPage(page));
    }

    @Override
    public R saveOrUpdateItem(SysRole sysRole) {
        List<Integer> deptIds = new ArrayList<>();
        SysUser sysUser = sysUserMapper.selectById(SecurityUtils.userId());

        // 全部
        if (sysRole.getScopeType().equals(DataScopeTypeEnum.ALL)) {
            sysRole.setScope(new Integer[]{});
            sysRole.setScope(deptIds.toArray(new Integer[0]));
        }

        // 本级
        if (sysRole.getScopeType().equals(DataScopeTypeEnum.THIS_LEVEL)) {
            deptIds.add(sysUser.getDeptId());
            sysRole.setScope(deptIds.toArray(new Integer[0]));
        }

        // 本级, 下级
        if (sysRole.getScopeType().equals(DataScopeTypeEnum.THIS_LEVEL_CHILDREN)) {
            deptIds.add(sysUser.getDeptId()); // 本级
            List<Tree<Integer>> tree = sysDeptService.tree(sysUser.getDeptId()); // 下级
            treeToList(deptIds, tree);
            sysRole.setScope(deptIds.toArray(new Integer[0]));
        }

        // 自定义时, scope 不能空
        if (sysRole.getScopeType().equals(DataScopeTypeEnum.CUSTOMIZE)) {
            if (sysRole.getScope() == null || sysRole.getScope().length == 0) {
                throw new RuntimeException("自定义权限范围时,必须至少包含一个范围");
            }
        }

        saveOrUpdate(sysRole);

        // 更新角色权限
        if (sysRole.getAuthorities() != null) {
            List<SysRoleAuthority> roleMenuList = Arrays.stream(sysRole.getAuthorities()).map(menuId -> {
                SysRoleAuthority roleMenu = new SysRoleAuthority();
                roleMenu.setRoleId(sysRole.getId());
                roleMenu.setAuthorityId(menuId);
                return roleMenu;
            }).collect(Collectors.toList());

            sysRoleAuthorityMapper.delete(Wrappers.<SysRoleAuthority>query().lambda()
                    .eq(SysRoleAuthority::getRoleId, sysRole.getId()));
            sysRoleAuthorityService.saveBatch(roleMenuList);
        }

        return R.ok();
    }

    private void treeToList(List<Integer> list, List<Tree<Integer>> tree) {
        for (Tree<Integer> node : tree) {
            list.add(node.getId());
            if (node.getChildren() != null && node.getChildren().size() > 0) {
                treeToList(list, node.getChildren());
            }
        }
    }
}
