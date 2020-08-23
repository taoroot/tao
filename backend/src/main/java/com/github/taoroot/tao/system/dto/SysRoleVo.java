package com.github.taoroot.tao.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysRoleVo {
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;

    private String role;

    private String scope;

    private String description;

    private Integer[] authorities;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
