package cn.flizi.boot.tao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 * <p>
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 * if not set alwaysShow, when item has more than one children route,
 * it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
 * roles: ['admin','editor']    control the page roles (you can set multiple roles)
 * title: 'title'               the name show in sidebar and breadcrumb (recommend set)
 * icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
 * breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
 * activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
 * }
 */
@TableName("authorities")
@EqualsAndHashCode(callSuper = true)
@Data
public class SysAuthority extends Model<SysAuthority> {

    @TableId(type = IdType.INPUT)
    private Integer id;

    private String path;

    private String component;

    private Boolean hidden;

    private Boolean alwaysShow;

    private String redirect;

    private String name;

    private String title;

    private String authority;

    private String icon;

    private Boolean breadcrumb;

    private Integer parentId;

    private Integer weight;

    private Integer type;
}
