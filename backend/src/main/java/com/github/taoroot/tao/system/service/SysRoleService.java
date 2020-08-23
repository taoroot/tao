package com.github.taoroot.tao.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.taoroot.tao.system.dto.SysRoleVo;
import com.github.taoroot.tao.system.entity.SysRole;
import com.github.taoroot.tao.utils.R;

public interface SysRoleService extends IService<SysRole> {

    R getPage(Page<SysRole> page);

    R updateItem(SysRoleVo sysRoleVo);
}
