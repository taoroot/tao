package cn.flizi.boot.tao.upms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("role_authority")
@EqualsAndHashCode(callSuper = true)
public class SysRoleAuthority extends Model<SysRoleAuthority> {

	private static final long serialVersionUID = 1L;

	private Integer roleId;

	private Integer authorityId;
}
