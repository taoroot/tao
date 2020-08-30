package cn.flizi.boot.tao.upms.controller;

import cn.flizi.boot.tao.upms.service.SysRoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.flizi.boot.tao.upms.entity.SysRole;
import cn.flizi.boot.tao.utils.R;
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
        return R.ok(sysRoleService.saveOrUpdateItem(sysRole));
    }

    @DeleteMapping("/role")
    public R delItem(@RequestParam List<Integer> ids) {
        return R.ok(sysRoleService.removeByIds(ids));
    }

    @PutMapping("/role")
    public R updateItem(@RequestBody SysRole sysRole) {
        return sysRoleService.saveOrUpdateItem(sysRole);
    }

    @GetMapping("/roles")
    public R getPage(Page<SysRole> page) {
        return sysRoleService.getPage(page);
    }
}
