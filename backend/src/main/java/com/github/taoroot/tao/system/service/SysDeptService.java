package com.github.taoroot.tao.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.taoroot.tao.system.entity.SysDept;
import com.github.taoroot.tao.utils.R;

public interface SysDeptService extends IService<SysDept> {

    R tree();
}
