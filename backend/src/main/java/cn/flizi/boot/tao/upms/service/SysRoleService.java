package cn.flizi.boot.tao.upms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.flizi.boot.tao.upms.entity.SysRole;
import cn.flizi.boot.tao.utils.R;

public interface SysRoleService extends IService<SysRole> {

    R getPage(Page<SysRole> page);

    R saveOrUpdateItem(SysRole sysRole);
}
