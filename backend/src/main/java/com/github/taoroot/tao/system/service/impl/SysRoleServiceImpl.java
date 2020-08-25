package com.github.taoroot.tao.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.security.SecurityUtils;
import com.github.taoroot.tao.system.datascope.DataScopeTypeEnum;
import com.github.taoroot.tao.system.dto.SysRoleVo;
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
    public R saveOrUpdateItem(SysRoleVo sysRoleVo) {
        SysRole sysRole = new SysRole();
        sysRole.setId(sysRoleVo.getId());
        sysRole.setScopeType(sysRoleVo.getScopeType());
        sysRole.setScope(sysRoleVo.getScope());

        if (sysRoleVo.getId() == null) {
            sysRole.setRole(sysRoleVo.getRole());
        }

        List<Integer> deptIds = new ArrayList<>();
        SysUser sysUser = sysUserMapper.selectById(SecurityUtils.userId());

        // 全部
        if (sysRole.getScopeType().equals(DataScopeTypeEnum.ALL)) {
            sysRole.setScope(new Integer[]{});
        }

        // 本级
        if (sysRole.getScopeType().equals(DataScopeTypeEnum.THIS_LEVEL)) {
            deptIds.add(sysUser.getDeptId());
        }

        // 本级, 下级
        if (sysRole.getScopeType().equals(DataScopeTypeEnum.THIS_LEVEL_CHILDREN)) {
            deptIds.add(sysUser.getDeptId()); // 本级
            List<Tree<Integer>> tree = sysDeptService.tree(sysUser.getDeptId()); // 下级
            treeToList(deptIds, tree);
        }

        sysRole.setScope(deptIds.toArray(new Integer[0]));
        saveOrUpdate(sysRole);

        // 更新角色权限
        if (sysRoleVo.getAuthorities() != null) {
            List<SysRoleAuthority> roleMenuList = Arrays.stream(sysRoleVo.getAuthorities()).map(menuId -> {
                SysRoleAuthority roleMenu = new SysRoleAuthority();
                roleMenu.setRoleId(sysRoleVo.getId());
                roleMenu.setAuthorityId(menuId);
                return roleMenu;
            }).collect(Collectors.toList());

            sysRoleAuthorityMapper.delete(Wrappers.<SysRoleAuthority>query().lambda()
                    .eq(SysRoleAuthority::getRoleId, sysRoleVo.getId()));
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
