package com.github.taoroot.tao.system.service.impl;


import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.entity.SysRoleAuthority;
import com.github.taoroot.tao.system.mapper.SysAuthorityMapper;
import com.github.taoroot.tao.system.mapper.SysRoleAuthorityMapper;
import com.github.taoroot.tao.system.service.SysRoleAuthorityService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class SysRoleAuthorityServiceImpl extends ServiceImpl<SysRoleAuthorityMapper, SysRoleAuthority> implements SysRoleAuthorityService {

    private final SysAuthorityMapper sysAuthorityMapper;

    @Override
    public R updatePermission(Integer roleId, String authorityIds) {
        List<SysRoleAuthority> roleMenuList =
                Arrays.stream(authorityIds.split(","))
                        .map(Integer::parseInt).map(menuId -> {
                    SysRoleAuthority roleMenu = new SysRoleAuthority();
                    roleMenu.setRoleId(roleId);
                    roleMenu.setAuthorityId(menuId);
                    return roleMenu;
                }).collect(Collectors.toList());

        this.remove(Wrappers.<SysRoleAuthority>query().lambda()
                .eq(SysRoleAuthority::getRoleId, roleId));

        return R.ok(this.saveBatch(roleMenuList));
    }

    @Override
    public R getPermission(Integer roleId) {
        HashMap<String, Object> hashMap = new HashMap<>();

        List<Tree<Integer>> all = getTrees();

        hashMap.put("authorityData", all);
        hashMap.put("checkedKeys", sysAuthorityMapper.getIdsByRole(roleId));

        return R.ok(hashMap);
    }

    public List<Tree<Integer>> getTrees() {
        List<SysAuthority> sysAuthorities = sysAuthorityMapper.selectList(Wrappers.emptyWrapper());
        return TreeUtil.build(sysAuthorities, 0,
                (treeNode, tree) -> {
                    tree.setId(treeNode.getId());
                    tree.setParentId(treeNode.getParentId());
                    tree.setWeight(treeNode.getWeight());
                    tree.setName(treeNode.getName());
                    tree.putExtra("name", treeNode.getTitle());
                });
    }
}
