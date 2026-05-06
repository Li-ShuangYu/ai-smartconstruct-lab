package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.SysFeedback;
import com.smartconstruct.backend_core.service.IFeedbackService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/feedbacks")
public class AdminFeedbackController {

    private final IFeedbackService feedbackService;

    public AdminFeedbackController(IFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public ApiResult<PageResult<SysFeedback>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        LambdaQueryWrapper<SysFeedback> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(SysFeedback::getCreatedAt);
        Page<SysFeedback> p = feedbackService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResult<SysFeedback> detail(@PathVariable Long id) {
        return ApiResult.ok(feedbackService.getById(id));
    }

    @OperationLog(action = "删除反馈")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        feedbackService.removeById(id);
        return ApiResult.ok();
    }
}
