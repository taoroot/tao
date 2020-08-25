package com.github.taoroot.tao.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.entity.SysDept;
import com.github.taoroot.tao.system.mapper.SysDeptMapper;
import com.github.taoroot.tao.system.service.SysDeptService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<Tree<Integer>> tree(Integer parentId) {
        List<SysDept> list = baseMapper.selectList(Wrappers.emptyWrapper());
        return TreeUtil.build(list, parentId, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setWeight(treeNode.getWeight());
            tree.setName(treeNode.getName());
            tree.putExtra("name", treeNode.getName());
        });
    }
}
