package cn.flizi.boot.tao.upms.service.impl;


import cn.flizi.boot.tao.upms.entity.SysAuthority;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.flizi.boot.tao.upms.entity.SysRoleAuthority;
import cn.flizi.boot.tao.upms.mapper.SysAuthorityMapper;
import cn.flizi.boot.tao.upms.mapper.SysRoleAuthorityMapper;
import cn.flizi.boot.tao.upms.service.SysAuthorityService;
import cn.flizi.boot.tao.utils.R;
import cn.flizi.boot.tao.utils.TreeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;


@Service
@AllArgsConstructor
public class SysAuthorityServiceImpl extends ServiceImpl<SysAuthorityMapper, SysAuthority> implements SysAuthorityService {

    private final SysAuthorityMapper sysAuthorityMapper;
    private final SysRoleAuthorityMapper sysRoleAuthorityMapper;

    @Override
    public R getTree(String title, Boolean hidden) {
        LambdaQueryWrapper<SysAuthority> query = Wrappers.lambdaQuery();

        if (hidden != null) {
            query.eq(SysAuthority::getHidden, hidden);
        }

        if (!StringUtils.isEmpty(title)) {
            query.like(SysAuthority::getTitle, title);
        }

        List<SysAuthority> sysAuthorities = sysAuthorityMapper.selectList(query);

        List<Tree<Integer>> authorityData = TreeUtil.build(sysAuthorities, TreeUtils.ROOT_PARENT_ID, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setWeight(treeNode.getWeight());
            tree.setName(treeNode.getName());
            tree.putExtra("path", treeNode.getPath());
            tree.putExtra("type", treeNode.getType());
            tree.putExtra("component", treeNode.getComponent());
            tree.putExtra("hidden", treeNode.getHidden());
            tree.putExtra("alwaysShow", treeNode.getAlwaysShow());
            tree.putExtra("redirect", treeNode.getRedirect());
            tree.putExtra("title", treeNode.getTitle());
            tree.putExtra("icon", treeNode.getIcon());
            tree.putExtra("authority", treeNode.getAuthority());
            tree.putExtra("breadcrumb", treeNode.getBreadcrumb());
        });

        if (authorityData.size() == 0) {
            return R.ok(sysAuthorities);
        }

        return R.ok(authorityData);
    }

    @Override
    public boolean removeById(Serializable id) {
        Integer count = sysRoleAuthorityMapper.selectCount(Wrappers.<SysRoleAuthority>lambdaQuery()
                .eq(SysRoleAuthority::getAuthorityId, id));
        if (count > 0) {
            throw new RuntimeException("资源被角色绑定,请先解绑");
        }
        return super.removeById(id);
    }

    @Override
    public boolean saveOrUpdate(SysAuthority entity) {
        return super.saveOrUpdate(entity);
    }
}
