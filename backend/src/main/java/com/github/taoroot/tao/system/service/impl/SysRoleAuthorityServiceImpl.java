package com.github.taoroot.tao.system.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.entity.SysRoleAuthority;
import com.github.taoroot.tao.system.mapper.SysRoleAuthorityMapper;
import com.github.taoroot.tao.system.service.SysRoleAuthorityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SysRoleAuthorityServiceImpl extends ServiceImpl<SysRoleAuthorityMapper, SysRoleAuthority> implements SysRoleAuthorityService {

}
