package com.github.taoroot.tao.system.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.taoroot.tao.system.entity.SysRoleAuthority;
import com.github.taoroot.tao.utils.R;

import java.util.List;

public interface SysRoleAuthorityService extends IService<SysRoleAuthority> {

    R updatePermission(Integer roleId, String authorityIds);

    R getPermission(Integer roleId);

    List<Tree<Integer>> getTrees();
}
