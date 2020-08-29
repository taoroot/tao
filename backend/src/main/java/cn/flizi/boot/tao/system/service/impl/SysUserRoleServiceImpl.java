package cn.flizi.boot.tao.system.service.impl;


import cn.flizi.boot.tao.system.entity.SysUserRole;
import cn.flizi.boot.tao.system.service.SysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.flizi.boot.tao.system.mapper.SysUserRoleMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

}
