package com.github.taoroot.tao.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.entity.SysDept;
import com.github.taoroot.tao.system.mapper.SysDeptMapper;
import com.github.taoroot.tao.system.service.SysDeptService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@AllArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<Tree<Integer>> tree(Integer parentId) {
        List<SysDept> list = baseMapper.selectList(Wrappers.emptyWrapper());
        return getTrees(parentId, list);
    }

    @Override
    public R tree(Integer parentId, String name) {
        LambdaQueryWrapper<SysDept> queryWrapper = Wrappers.lambdaQuery();

        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like(SysDept::getName, name);
        }

        List<SysDept> list = baseMapper.selectList(queryWrapper);
        List<Tree<Integer>> tree = getTrees(parentId, list);
        if (tree.size() != 0) {
            return R.ok(tree);
        }
        return R.ok(list);
    }

    private List<Tree<Integer>> getTrees(Integer parentId, List<SysDept> list) {
        return TreeUtil.build(list, parentId, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setWeight(treeNode.getWeight());
            tree.setName(treeNode.getName());
            tree.putExtra("name", treeNode.getName());
            tree.putExtra("email", treeNode.getEmail());
            tree.putExtra("enabled", treeNode.getEnabled());
            tree.putExtra("leader", treeNode.getLeader());
            tree.putExtra("phone", treeNode.getPhone());
        });
    }
}
