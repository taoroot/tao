package com.github.taoroot.tao.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.taoroot.tao.system.entity.SysRole;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/role")
public class RoleController {

    @PutMapping("/menu")
    public R saveRoleMenus(Integer roleId, @RequestParam(value = "menuIds", required = false) String menuIds) {
        return R.ok();
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable Integer id) {
        return R.ok();
    }

    @PostMapping
    public R save(@RequestBody SysRole sysRole) {
        return R.ok();
    }

    @PutMapping
    public R update(@RequestBody SysRole sysRole) {
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R removeById(@PathVariable Integer id) {
        return R.ok();
    }

    @GetMapping("/list")
    public R listRoles() {
        return R.ok();
    }

    @GetMapping("/page")
    public R getRolePage(Page page) {
        return R.ok();
    }
}
