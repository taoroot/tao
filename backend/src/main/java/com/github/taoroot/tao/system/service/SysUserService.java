package com.github.taoroot.tao.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.taoroot.tao.system.dto.SysUserVO;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.utils.R;

public interface SysUserService extends IService<SysUser> {

    R userInfo();

    R getPage(Page<SysUser> page);

    R saveOrUpdateItem(SysUserVO sysUser);
}
