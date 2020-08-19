package com.github.taoroot.tao.utils;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.github.taoroot.tao.system.entity.SysAuthority;

import java.util.HashMap;
import java.util.List;

public class TreeUtils {

    public static List<Tree<Integer>> toTree(List<SysAuthority> sysAuthorities) {
        return TreeUtil.build(sysAuthorities, 0,
                (treeNode, tree) -> {
                    common(treeNode, tree);
                    tree.put("component", treeNode.getComponent());
                    HashMap<String, Object> meta = new HashMap<>();
                    meta.put("title", treeNode.getTitle());
                    meta.put("icon", treeNode.getIcon());
                    meta.put("breadcrumb", treeNode.getBreadcrumb());
                    tree.putExtra("meta", meta);
                });
    }

    public static List<Tree<Integer>> toTree1(List<SysAuthority> sysAuthorities) {
        return TreeUtil.build(sysAuthorities, 0,
                (treeNode, tree) -> {
                    common(treeNode, tree);
                    tree.putExtra("component", treeNode.getComponent());
                    tree.putExtra("title", treeNode.getTitle());
                    tree.putExtra("icon", treeNode.getIcon());
                    tree.putExtra("breadcrumb", treeNode.getBreadcrumb());
                });
    }

    private static void common(SysAuthority treeNode, Tree<Integer> tree) {
        tree.setId(treeNode.getId());
        tree.setParentId(treeNode.getParentId());
        tree.setWeight(treeNode.getWeight());
        tree.setName(treeNode.getName());
        tree.putExtra("path", treeNode.getPath());
        tree.putExtra("hidden", treeNode.getHidden());
        tree.putExtra("alwaysShow", treeNode.getAlwaysShow());
        tree.putExtra("redirect", treeNode.getRedirect());
    }
}
