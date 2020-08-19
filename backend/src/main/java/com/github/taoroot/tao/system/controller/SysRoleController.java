package com.github.taoroot.tao.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.taoroot.tao.system.entity.SysRole;
import com.github.taoroot.tao.system.service.SysRoleAuthorityService;
import com.github.taoroot.tao.system.service.SysRoleService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping
public class SysRoleController {

    private final SysRoleService sysRoleService;

    private final SysRoleAuthorityService sysRoleAuthorityService;

    @PostMapping("/role")
    public R saveItem(@RequestBody SysRole sysRole) {
        return R.ok(sysRoleService.save(sysRole));
    }

    @DeleteMapping("/role")
    public R delItem(@RequestParam List<Integer> ids) {
        return R.ok(sysRoleService.removeByIds(ids));
    }

    @PutMapping("/role")
    public R updateItem(@RequestBody SysRole sysRole) {
        return R.ok(sysRoleService.updateById(sysRole));
    }

    @GetMapping("/roles")
    public R getPage(Page<SysRole> page) {
        return R.ok(sysRoleService.page(page));
    }

    @GetMapping("/role/{roleId}/authorities")
    public R getPermission(@PathVariable Integer roleId) {
        return sysRoleAuthorityService.getPermission(roleId);
    }

    @PutMapping("/role/{roleId}/authorities")
    public R updatePermission(@PathVariable Integer roleId, String authorityIds) {
        return sysRoleAuthorityService.updatePermission(roleId, authorityIds);
    }
}
