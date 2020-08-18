package com.github.taoroot.tao.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author : zhiyi
 * Date: 2020/2/11
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    List<String> roles(@Param("userId") Integer userId);

    List<SysAuthority> authorities(@Param("userId") Integer userId, @Param("type") Integer type);
}
