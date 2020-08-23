package com.github.taoroot.tao.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.taoroot.tao.system.dto.SysRoleVo;
import com.github.taoroot.tao.system.entity.SysRole;
import com.github.taoroot.tao.system.service.SysRoleService;
import com.github.taoroot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @PostMapping("/role")
    public R saveItem(@RequestBody SysRole sysRole) {
        return R.ok(sysRoleService.save(sysRole));
    }

    @DeleteMapping("/role")
    public R delItem(@RequestParam List<Integer> ids) {
        return R.ok(sysRoleService.removeByIds(ids));
    }

    @PutMapping("/role")
    public R updateItem(@RequestBody SysRoleVo sysRoleVo) {
        return sysRoleService.updateItem(sysRoleVo);
    }

    @GetMapping("/roles")
    public R getPage(Page<SysRole> page) {
        return sysRoleService.getPage(page);
    }

}
