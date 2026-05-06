package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.SysOperationLog;
import com.smartconstruct.backend_core.service.IOperationLogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

    private final IOperationLogService operationLogService;

    public AdminLogController(IOperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResult<PageResult<SysOperationLog>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<SysOperationLog> qw = new LambdaQueryWrapper<>();
        if (actionType != null && !actionType.isEmpty()) qw.eq(SysOperationLog::getActionType, actionType);
        if (userId != null) qw.eq(SysOperationLog::getUserId, userId);
        qw.orderByDesc(SysOperationLog::getCreatedAt);
        Page<SysOperationLog> p = operationLogService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }
}
