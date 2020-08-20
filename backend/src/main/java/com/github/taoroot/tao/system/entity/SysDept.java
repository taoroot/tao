package com.github.taoroot.tao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.github.taoroot.tao.utils.IntegerToIntArrayHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "depts", autoResultMap = true)
public class SysDept extends Model<SysDept> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType= JdbcType.ARRAY)
    private Integer[] path;

    private Integer parentId;

    private Integer weight;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
