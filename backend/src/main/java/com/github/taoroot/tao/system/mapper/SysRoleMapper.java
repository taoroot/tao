package com.github.taoroot.tao.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.taoroot.tao.system.dto.SysRoleVo;
import com.github.taoroot.tao.system.entity.SysRole;

import java.util.List;

/**
 * @author : zhiyi
 * Date: 2020/2/11
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    IPage<SysRoleVo> getPage(Page<SysRole> page);

    List<Integer> selectAuthoritiesByRole(Integer roleId);
}
