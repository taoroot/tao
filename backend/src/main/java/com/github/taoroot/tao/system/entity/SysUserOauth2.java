package com.github.taoroot.tao.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("用户")
@TableName("user_oauth2")
public class SysUserOauth2 extends Model<SysUserOauth2> {
    private String clientRegistrationId;
    private String principalName;
    private Integer userId;
    private LocalDateTime createdAt;
}
