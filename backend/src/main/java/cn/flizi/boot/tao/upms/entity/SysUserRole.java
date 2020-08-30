package cn.flizi.boot.tao.upms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("user_role")
@EqualsAndHashCode(callSuper = true)
public class SysUserRole extends Model<SysUserRole> {

    private static final long serialVersionUID = 1L;

    private Integer userId;

    private Integer roleId;
}
