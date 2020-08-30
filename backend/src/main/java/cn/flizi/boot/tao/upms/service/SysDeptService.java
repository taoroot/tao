package cn.flizi.boot.tao.upms.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.flizi.boot.tao.upms.entity.SysDept;
import cn.flizi.boot.tao.utils.R;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    List<Tree<Integer>> tree(Integer parentId);

    R tree(Integer parentId, String name);
}
