package cn.flizi.boot.tao.upms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import cn.flizi.boot.tao.datascope.DataScopeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

@Data
@TableName("roles")
@EqualsAndHashCode(callSuper = true)
public class SysRole extends Model<SysRole> {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String role;

    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.ARRAY)
    private Integer[] scope;

    private DataScopeTypeEnum scopeType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer[] authorities;
}
