package com.github.taoroot.tao.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.utils.R;

public interface SysUserService extends IService<SysUser> {

    R userInfo();

    R getPage(Page<SysUser> page, String username, String phone, Integer deptId, Boolean enabled);

    R saveOrUpdateItem(SysUser sysUser);
}
