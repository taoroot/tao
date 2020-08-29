package com.github.taoroot.tao.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.taoroot.tao.datascope.DataScope;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author : zhiyi
 * Date: 2020/2/11
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    List<Integer> roleIds(@Param("userId") Integer userId);

    List<SysRole> roles(@Param("userId") Integer userId);

    List<SysAuthority> authorities(@Param("userId") Integer userId, @Param("type") Integer type);

    IPage<SysUser> getPage(@Param("page") Page<SysUser> page, @Param("dataScope") DataScope dataScope,
                             @Param("username") String username,
                             @Param("phone") String phone,
                             @Param("deptId") Integer deptId,
                             @Param("enabled") Boolean enabled);
}
