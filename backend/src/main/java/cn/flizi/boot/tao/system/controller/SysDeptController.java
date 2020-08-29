package cn.flizi.boot.tao.system.controller;

import cn.flizi.boot.tao.system.entity.SysDept;
import cn.flizi.boot.tao.system.service.SysDeptService;
import cn.flizi.boot.tao.utils.R;
import cn.flizi.boot.tao.utils.TreeUtils;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    @PostMapping("/dept")
    public R saveItem(@RequestBody SysDept sysDept) {
        return R.ok(sysDeptService.save(sysDept));
    }

    @DeleteMapping("/dept")
    public R delItem(@RequestParam List<Integer> ids) {
        return R.ok(sysDeptService.removeByIds(ids));
    }

    @GetMapping("/dept/{id}")
    public R delItem(@PathVariable Integer id) {
        return R.ok(sysDeptService.getById(id));
    }

    @PutMapping("/dept")
    public R updateItem(@RequestBody SysDept sysDept) {
        if (sysDept.getParentId().equals(sysDept.getId())) {
            throw new IllegalArgumentException("参数有误, 不能设置自己为上一级");
        }
        return R.ok(sysDeptService.updateById(sysDept));
    }

    @GetMapping("/depts")
    public R getPage(@RequestParam(defaultValue = "" + TreeUtils.ROOT_PARENT_ID) Integer parentId,
                     @RequestParam(required = false) String name) {
        return sysDeptService.tree(parentId, name);
    }
}

