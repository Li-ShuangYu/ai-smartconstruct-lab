package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.WfNodeDef;
import com.smartconstruct.backend_core.service.INodeDefService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 编排节点定义管理
 */
@RestController
@RequestMapping("/api/admin/nodes")
public class AdminNodeController {

    private final INodeDefService nodeDefService;

    public AdminNodeController(INodeDefService nodeDefService) {
        this.nodeDefService = nodeDefService;
    }

    /** 查询（无分页） */
    @GetMapping
    public ApiResult<List<WfNodeDef>> list() {
        LambdaQueryWrapper<WfNodeDef> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(WfNodeDef::getNodeType);
        return ApiResult.ok(nodeDefService.list(qw));
    }

    @GetMapping("/{id}")
    public ApiResult<WfNodeDef> detail(@PathVariable Long id) {
        return ApiResult.ok(nodeDefService.getById(id));
    }

    /** 新增：校验必填、唯一性 */
    @OperationLog(action = "新增编排节点API")
    @PostMapping
    public ApiResult<Void> create(@RequestBody WfNodeDef node) {
        if (node.getNodeType() == null || node.getNodeType().isBlank()) {
            return ApiResult.error("节点类型不能为空");
        }
        if (node.getNodeName() == null || node.getNodeName().isBlank()) {
            return ApiResult.error("节点名称不能为空");
        }
        long dup = nodeDefService.count(new LambdaQueryWrapper<WfNodeDef>()
                .eq(WfNodeDef::getNodeType, node.getNodeType()));
        if (dup > 0) {
            return ApiResult.error("节点类型已存在");
        }
        if (node.getIsActive() == null) {
            node.setIsActive(1);
        }
        nodeDefService.save(node);
        return ApiResult.ok();
    }

    /** 修改：禁止修改 START/END，校验必填 */
    @OperationLog(action = "编辑编排节点")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody WfNodeDef node) {
        WfNodeDef exists = nodeDefService.getById(id);
        if (exists == null) {
            return ApiResult.error("节点不存在");
        }
        if ("START".equals(exists.getNodeType()) || "END".equals(exists.getNodeType())) {
            return ApiResult.error("开始节点和结束节点不允许修改");
        }
        if (node.getNodeType() != null && !node.getNodeType().isBlank()
                && !node.getNodeType().equals(exists.getNodeType())) {
            long dup = nodeDefService.count(new LambdaQueryWrapper<WfNodeDef>()
                    .eq(WfNodeDef::getNodeType, node.getNodeType())
                    .ne(WfNodeDef::getId, id));
            if (dup > 0) {
                return ApiResult.error("节点类型已存在");
            }
        }
        node.setId(id);
        nodeDefService.updateById(node);
        return ApiResult.ok();
    }

    /** 删除：二次确认由前端弹窗实现 */
    @OperationLog(action = "删除编排节点")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        WfNodeDef exists = nodeDefService.getById(id);
        if (exists == null) {
            return ApiResult.error("节点不存在");
        }
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
