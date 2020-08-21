package com.github.taoroot.tao.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.entity.SysDept;
import com.github.taoroot.tao.system.mapper.SysDeptMapper;
import com.github.taoroot.tao.system.service.SysDeptService;
import com.github.taoroot.tao.utils.R;
import com.github.taoroot.tao.utils.TreeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public R tree() {
        List<SysDept> list = baseMapper.selectList(Wrappers.emptyWrapper());
        List<Tree<Integer>> result = TreeUtils.deptTree(list);
        return R.ok(result);
    }
}
