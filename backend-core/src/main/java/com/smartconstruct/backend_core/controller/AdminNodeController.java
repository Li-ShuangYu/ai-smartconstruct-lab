package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.WfNodeDef;
import com.smartconstruct.backend_core.service.INodeDefService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/nodes")
public class AdminNodeController {

    private final INodeDefService nodeDefService;

    public AdminNodeController(INodeDefService nodeDefService) {
        this.nodeDefService = nodeDefService;
    }

    @GetMapping
    public ApiResult<PageResult<WfNodeDef>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        LambdaQueryWrapper<WfNodeDef> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(WfNodeDef::getNodeCode);
        Page<WfNodeDef> p = nodeDefService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResult<WfNodeDef> detail(@PathVariable Long id) {
        return ApiResult.ok(nodeDefService.getById(id));
    }

    @OperationLog(action = "新增编排节点")
    @PostMapping
    public ApiResult<Void> create(@RequestBody WfNodeDef node) {
        nodeDefService.save(node);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑编排节点")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody WfNodeDef node) {
        node.setId(id);
        nodeDefService.updateById(node);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除编排节点")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        nodeDefService.removeById(id);
        return ApiResult.ok();
    }

    @OperationLog(action = "切换节点开关")
    @PutMapping("/{id}/toggle")
    public ApiResult<Void> toggle(@PathVariable Long id, @RequestParam Integer isActive) {
        WfNodeDef node = new WfNodeDef();
        node.setId(id);
        node.setIsActive(isActive);
        nodeDefService.updateById(node);
        return ApiResult.ok();
    }
}
