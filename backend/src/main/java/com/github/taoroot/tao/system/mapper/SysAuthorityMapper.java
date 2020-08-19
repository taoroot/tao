package com.github.taoroot.tao.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.taoroot.tao.system.entity.SysAuthority;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author : zhiyi
 * Date: 2020/2/11
 */
public interface SysAuthorityMapper extends BaseMapper<SysAuthority> {

    List<Integer> getIdsByRole(@Param("roleId") Integer roleId);
}
