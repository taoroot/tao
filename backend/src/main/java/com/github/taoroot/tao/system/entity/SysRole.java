package com.github.taoroot.tao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("roles")
@EqualsAndHashCode(callSuper = true)
public class SysRole extends Model<SysRole> {
	private static final long serialVersionUID = 1L;

	@TableId(value = "role_id", type = IdType.AUTO)
	private Integer roleId;

	private String roleName;

	private String roleDesc;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;
}
