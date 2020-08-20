package com.github.taoroot.tao.system.dto;

import lombok.Data;

@Data
public class SysUserPageVO {

    private Integer id;

    private String username;

    private String phone;

    private String avatar;

    private String enabled;

    private String deptId;

    private String deptName;
}
