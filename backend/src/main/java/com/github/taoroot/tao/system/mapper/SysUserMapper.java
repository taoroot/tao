package com.github.taoroot.tao.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.taoroot.tao.system.datascope.DataScope;
import com.github.taoroot.tao.system.dto.SysUserPageVO;
import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.entity.SysRole;
import com.github.taoroot.tao.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author : zhiyi
 * Date: 2020/2/11
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    List<SysRole> roles(@Param("userId") Integer userId);

    List<SysAuthority> authorities(@Param("userId") Integer userId, @Param("type") Integer type);

    IPage<SysUserPageVO> getPage(Page<SysUser> page, DataScope dataScope);
}
