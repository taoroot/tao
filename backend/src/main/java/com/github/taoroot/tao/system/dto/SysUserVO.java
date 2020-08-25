package com.github.taoroot.tao.system.dto;

import lombok.Data;

@Data
public class SysUserVO {

    private Integer id;

    private String username;

    private String nickname;

    private String phone;

    private String avatar;

    private Boolean enabled;

    private Integer deptId;

    private String deptName;

    private Integer[] roles;
}
