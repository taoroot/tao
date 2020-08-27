package com.github.taoroot.tao.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("user_oauth2")
@EqualsAndHashCode(callSuper = true)
public class SysUserOauth2 extends Model<SysUserOauth2> {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String clientRegistrationId;
    private String principalName;
    private String nickname;
    private String avatar;
    private Integer userId;
    private LocalDateTime createdAt;
}
