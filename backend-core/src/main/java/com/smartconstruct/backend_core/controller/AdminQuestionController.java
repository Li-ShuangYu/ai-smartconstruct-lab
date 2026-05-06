package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizQuestion;
import com.smartconstruct.backend_core.entity.BizQuestionBank;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import com.smartconstruct.backend_core.service.IQuestionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
public class AdminQuestionController {

    private final IQuestionBankService bankService;
    private final IQuestionService questionService;

    public AdminQuestionController(IQuestionBankService bankService, IQuestionService questionService) {
        this.bankService = bankService;
        this.questionService = questionService;
    }

    // ===== 题库 CRUD =====
    @GetMapping("/question-banks")
    public ApiResult<PageResult<BizQuestionBank>> listBanks(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        LambdaQueryWrapper<BizQuestionBank> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(BizQuestionBank::getCreatedAt);
        Page<BizQuestionBank> p = bankService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/question-banks/{id}")
    public ApiResult<BizQuestionBank> bankDetail(@PathVariable Long id) {
        return ApiResult.ok(bankService.getById(id));
    }

    @OperationLog(action = "新增题库")
    @PostMapping("/question-banks")
    public ApiResult<Void> createBank(@RequestBody BizQuestionBank bank) {
        bank.setCreatedAt(LocalDateTime.now());
        bank.setUpdatedAt(LocalDateTime.now());
        bankService.save(bank);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑题库")
    @PutMapping("/question-banks/{id}")
    public ApiResult<Void> updateBank(@PathVariable Long id, @RequestBody BizQuestionBank bank) {
        bank.setId(id);
        bank.setUpdatedAt(LocalDateTime.now());
        bankService.updateById(bank);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除题库")
    @DeleteMapping("/question-banks/{id}")
    public ApiResult<Void> deleteBank(@PathVariable Long id) {
        bankService.removeById(id);
        return ApiResult.ok();
    }

    @OperationLog(action = "切换题库共享状态")
    @PutMapping("/question-banks/{id}/share")
    public ApiResult<Void> toggleShare(@PathVariable Long id, @RequestParam Integer isPublic) {
        BizQuestionBank bank = new BizQuestionBank();
        bank.setId(id);
        bank.setIsPublic(isPublic);
        bank.setUpdatedAt(LocalDateTime.now());
        bankService.updateById(bank);
        return ApiResult.ok();
    }

    // ===== 题目 CRUD =====
    @GetMapping("/questions")
    public ApiResult<PageResult<BizQuestion>> listQuestions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long bankId) {
        LambdaQueryWrapper<BizQuestion> qw = new LambdaQueryWrapper<>();
        if (bankId != null) qw.eq(BizQuestion::getBankId, bankId);
        qw.orderByAsc(BizQuestion::getSortNum);
        Page<BizQuestion> p = questionService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/questions/{id}")
    public ApiResult<BizQuestion> questionDetail(@PathVariable Long id) {
        return ApiResult.ok(questionService.getById(id));
    }

    @OperationLog(action = "新增题目")
    @PostMapping("/questions")
    public ApiResult<Void> createQuestion(@RequestBody BizQuestion question) {
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        questionService.save(question);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑题目")
    @PutMapping("/questions/{id}")
    public ApiResult<Void> updateQuestion(@PathVariable Long id, @RequestBody BizQuestion question) {
        question.setId(id);
        question.setUpdatedAt(LocalDateTime.now());
        questionService.updateById(question);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除题目")
    @DeleteMapping("/questions/{id}")
    public ApiResult<Void> deleteQuestion(@PathVariable Long id) {
        questionService.removeById(id);
        return ApiResult.ok();
    }
}
