package cn.flizi.boot.tao.system.service.impl;


import cn.flizi.boot.tao.system.service.SysRoleAuthorityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.flizi.boot.tao.system.entity.SysRoleAuthority;
import cn.flizi.boot.tao.system.mapper.SysRoleAuthorityMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SysRoleAuthorityServiceImpl extends ServiceImpl<SysRoleAuthorityMapper, SysRoleAuthority> implements SysRoleAuthorityService {


}
