package com.github.taoroot.tao.system.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.mapper.SysAuthorityMapper;
import com.github.taoroot.tao.system.service.SysAuthorityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SysAuthorityServiceImpl extends ServiceImpl<SysAuthorityMapper, SysAuthority> implements SysAuthorityService {

}
