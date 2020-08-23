package com.github.taoroot.tao.system.controller;

import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.service.SysAuthorityService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class SysAuthorityController {
    private final SysAuthorityService sysAuthorityService;

    @GetMapping(value = "/authorities")
    public R getTree(@RequestParam(defaultValue = "") String title, @RequestParam(required = false) Boolean hidden) {
        return sysAuthorityService.getTree(title, hidden);
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
