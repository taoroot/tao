package cn.flizi.boot.tao.upms.service;

import cn.flizi.boot.tao.upms.entity.SysUser;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.flizi.boot.tao.utils.R;

public interface SysUserService extends IService<SysUser> {

    R userInfo();

    R getPage(Page<SysUser> page, String username, String phone, Integer deptId, Boolean enabled);

    R saveOrUpdateItem(SysUser sysUser);
}
