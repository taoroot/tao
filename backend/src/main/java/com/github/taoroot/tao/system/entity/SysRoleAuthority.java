package com.github.taoroot.tao.system.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleAuthority extends Model<SysRoleAuthority> {

	private static final long serialVersionUID = 1L;

	private Integer roleId;

	private Integer authorityId;
}
