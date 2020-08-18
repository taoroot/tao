package com.github.taoroot.tao.system.controller;

import com.github.taoroot.tao.system.entity.SysAuthority;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/authority")
public class AuthorityController {

    @GetMapping
    public R getUserMenu() {
        return R.ok();
    }

    @GetMapping(value = "/tree")
    public R getTree() {
        return R.ok();
    }

    @GetMapping("/role/{roleId}")
    public R getRoleTree(@PathVariable Integer roleId) {
        return R.ok();
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable Integer id) {
        return R.ok();
    }

    @PostMapping
    public R save(@RequestBody SysAuthority sysAuthority) {
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R removeById(@PathVariable Integer id) {
        return R.ok();
    }

    @PutMapping
    public R update(@RequestBody SysAuthority sysAuthority) {
        return R.ok();
    }
}
