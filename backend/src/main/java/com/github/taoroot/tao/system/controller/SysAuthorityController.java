package com.github.taoroot.tao.system.controller;

import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.service.SysAuthorityService;
import com.github.taoroot.tao.system.service.SysRoleAuthorityService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping
public class SysAuthorityController {
    private final SysAuthorityService sysAuthorityService;
    private final SysRoleAuthorityService sysRoleAuthorityService;

    @GetMapping(value = "/authorities")
    public R getTree() {
        return R.ok(sysRoleAuthorityService.getTrees());
    }

    @GetMapping("/authority/{id}")
    public R getById(@PathVariable Integer id) {
        return R.ok(sysAuthorityService.getById(id));
    }

    @DeleteMapping("/authority/{id}")
    public R removeById(@PathVariable Integer id) {
        return R.ok(sysAuthorityService.removeById(id));
    }

    @PostMapping("/authority")
    public R save(@RequestBody SysAuthority sysAuthority) {
        return R.ok(sysAuthorityService.saveOrUpdate(sysAuthority));
    }

    @PutMapping("/authority")
    public R update(@RequestBody SysAuthority sysAuthority) {
        return R.ok(sysAuthorityService.saveOrUpdate(sysAuthority));
    }
}
