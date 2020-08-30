package cn.flizi.boot.tao.upms.mapper;

import cn.flizi.boot.tao.upms.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * @author : zhiyi
 * Date: 2020/2/11
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    IPage<SysRole> getPage(Page<SysRole> page);

    List<Integer> selectAuthoritiesByRole(Integer roleId);
}
