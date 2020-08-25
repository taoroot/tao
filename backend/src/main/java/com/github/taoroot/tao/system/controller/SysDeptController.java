package com.github.taoroot.tao.system.controller;

import com.github.taoroot.tao.system.entity.SysDept;
import com.github.taoroot.tao.system.service.SysDeptService;
import com.github.taoroot.tao.utils.R;
import com.github.taoroot.tao.utils.TreeUtils;
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
        return R.ok(sysDeptService.updateById(sysDept));
    }

    @GetMapping("/depts")
    public R getPage(@RequestParam(defaultValue = "" + TreeUtils.ROOT_PARENT_ID) Integer parentId) {
        return R.ok(sysDeptService.tree(parentId));
    }
}

