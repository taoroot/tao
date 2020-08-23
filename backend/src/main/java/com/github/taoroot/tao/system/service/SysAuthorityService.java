package com.github.taoroot.tao.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.utils.R;

public interface SysAuthorityService extends IService<SysAuthority> {
    R getTree(String title, Boolean hidden);
}
