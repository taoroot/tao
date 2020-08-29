package cn.flizi.boot.tao.system.controller;

import cn.flizi.boot.tao.system.entity.SysUser;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.flizi.boot.tao.system.service.SysUserService;
import cn.flizi.boot.tao.utils.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @PostMapping("/user")
    public R saveItem(@RequestBody SysUser sysUser) {
        return sysUserService.saveOrUpdateItem(sysUser);
    }

    @DeleteMapping("/user")
    public R delItem(@RequestParam List<Integer> ids) {
        return R.ok(sysUserService.removeByIds(ids));
    }

    @PutMapping("/user")
    public R updateItem(@RequestBody SysUser sysUser) {
        return sysUserService.saveOrUpdateItem(sysUser);
    }

    @GetMapping("/users")
    public R getPage(Page<SysUser> page,
                     @RequestParam(required = false) String username,
                     @RequestParam(required = false) String phone,
                     @RequestParam(required = false) Integer deptId,
                     @RequestParam(required = false) Boolean enabled) {
        return sysUserService.getPage(page, username, phone, deptId, enabled);
    }
}

