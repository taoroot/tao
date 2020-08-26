package com.github.taoroot.tao.system.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.taoroot.tao.system.entity.SysDept;
import com.github.taoroot.tao.utils.R;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    List<Tree<Integer>> tree(Integer parentId);

    R tree(Integer parentId, String name);
}
