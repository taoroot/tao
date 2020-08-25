package com.github.taoroot.tao.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.taoroot.tao.system.datascope.DataScope;
import com.github.taoroot.tao.system.entity.SysUser;
import com.github.taoroot.tao.system.mapper.SysUserMapper;
import com.github.taoroot.tao.system.service.SysUserService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @PostMapping("/user")
    public R saveItem(@RequestBody SysUser sysUser) {
        return R.ok(sysUserService.save(sysUser));
    }

    @DeleteMapping("/user")
    public R delItem(@RequestParam List<Integer> ids) {
        return R.ok(sysUserService.removeByIds(ids));
    }

    @PutMapping("/user")
    public R updateItem(@RequestBody SysUser sysUser) {
        return R.ok(sysUserService.updateById(sysUser));
    }

    @GetMapping("/users")
    public R getPage(Page<SysUser> page) {
        return sysUserService.getPage(page);
    }
}

