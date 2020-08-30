package cn.flizi.boot.tao.upms.service;

import cn.flizi.boot.tao.upms.entity.SysAuthority;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.flizi.boot.tao.utils.R;

public interface SysAuthorityService extends IService<SysAuthority> {
    R getTree(String title, Boolean hidden);
}
