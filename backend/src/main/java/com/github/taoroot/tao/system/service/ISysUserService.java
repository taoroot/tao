package com.github.taoroot.tao.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.utils.R;

public interface ISysUserService extends IService<SysUser> {

    R userInfo();

    Object userMenus();
}
