package com.github.taoroot.tao.system.controller;

import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.system.service.SysAuthorityService;
import com.github.taoroot.tao.utils.R;
import com.github.taoroot.tao.utils.TreeUtils;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping
public class SysAuthorityController {
    private final SysAuthorityService sysAuthorityService;

    @GetMapping
    public R getUserMenu() {
        return R.ok();
    }

    @GetMapping(value = "/authority/tree")
    public R getTree() {
        return R.ok(TreeUtils.toTree1(sysAuthorityService.list()));
    }

    @GetMapping("/authority/{id}")
    public R getById(@PathVariable Integer id) {
        return R.ok();
    }

    @DeleteMapping("/authority/{id}")
    public R removeById(@PathVariable Integer id) {
        return R.ok();
    }

    @PostMapping("/authority")
    public R save(@RequestBody SysAuthority sysAuthority) {
        return R.ok();
    }

    @PutMapping("/authority")
    public R update(@RequestBody SysAuthority sysAuthority) {
        return R.ok();
    }
}
