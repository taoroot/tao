package com.github.taoroot.tao.system.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.entity.SysUserRole;
import com.github.taoroot.tao.system.mapper.SysUserRoleMapper;
import com.github.taoroot.tao.system.service.SysUserRoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

}
