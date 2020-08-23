package com.github.taoroot.tao.system.service.impl;


import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.mapper.SysAuthorityMapper;
import com.github.taoroot.tao.system.service.SysAuthorityService;
import com.github.taoroot.tao.utils.R;
import com.github.taoroot.tao.utils.TreeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
@AllArgsConstructor
public class SysAuthorityServiceImpl extends ServiceImpl<SysAuthorityMapper, SysAuthority> implements SysAuthorityService {

    private final SysAuthorityMapper sysAuthorityMapper;

    @Override
    public R getTree(String title, Boolean hidden) {
        LambdaQueryWrapper<SysAuthority> query = Wrappers.lambdaQuery();

        if(hidden != null) {
            query.eq(SysAuthority::getHidden, hidden);
        }

        if(!StringUtils.isEmpty(title)) {
            query.like(SysAuthority::getTitle, title);
        }

        List<SysAuthority> sysAuthorities = sysAuthorityMapper.selectList(query);

        List<Tree<Integer>> authorityData = TreeUtil.build(sysAuthorities, TreeUtils.ROOT_PARENT_ID, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setWeight(treeNode.getWeight());
            tree.setName(treeNode.getName());
            tree.putExtra("path", treeNode.getPath());
            tree.putExtra("type", treeNode.getType());
            tree.putExtra("component", treeNode.getComponent());
            tree.putExtra("hidden", treeNode.getHidden());
            tree.putExtra("alwaysShow", treeNode.getAlwaysShow());
            tree.putExtra("redirect", treeNode.getRedirect());
            tree.putExtra("title", treeNode.getTitle());
            tree.putExtra("icon", treeNode.getIcon());
            tree.putExtra("authority", treeNode.getAuthority());
            tree.putExtra("breadcrumb", treeNode.getBreadcrumb());
        });

        if(authorityData.size() == 0) {
            return R.ok(sysAuthorities);
        }

        return R.ok(authorityData);
    }
}
