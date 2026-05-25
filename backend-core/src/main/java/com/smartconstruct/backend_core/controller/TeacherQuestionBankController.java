package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.QuestionBankVO;
import com.smartconstruct.backend_core.entity.BizQuestion;
import com.smartconstruct.backend_core.entity.BizQuestionBank;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import com.smartconstruct.backend_core.service.IQuestionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/question-banks")
public class TeacherQuestionBankController {

    private final IQuestionBankService bankService;
    private final IQuestionService questionService;

    public TeacherQuestionBankController(IQuestionBankService bankService, IQuestionService questionService) {
        this.bankService = bankService;
        this.questionService = questionService;
    }

    private Long getCurrentUserId() {
        return ((SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    @GetMapping
    public ApiResult<List<QuestionBankVO>> list() {
        Long userId = getCurrentUserId();
        List<BizQuestionBank> list = bankService.list(new LambdaQueryWrapper<BizQuestionBank>()
                .eq(BizQuestionBank::getTeacherId, userId).or()
                .eq(BizQuestionBank::getIsPublic, 1));
        List<QuestionBankVO> voList = list.stream().map(b -> {
            QuestionBankVO vo = new QuestionBankVO();
            vo.setId(b.getId());
            vo.setCreatorId(b.getTeacherId());
            vo.setBankName(b.getBankName());
            vo.setIsPublic(b.getIsPublic());
            vo.setCreatedAt(b.getCreatedAt());
            vo.setUpdatedAt(b.getUpdatedAt());
            vo.setIsCreator(b.getTeacherId() != null && b.getTeacherId().equals(userId));
            return vo;
        }).collect(Collectors.toList());
        return ApiResult.ok(voList);
    }

    @OperationLog(action = "新增题库")
    @PostMapping
    public ApiResult<Void> create(@RequestBody Map<String, Object> body) {
        String bankName = (String) body.get("bankName");
        Integer isPublic = body.get("isPublic") != null ? Integer.valueOf(body.get("isPublic").toString()) : 0;
        if (bankName == null || bankName.isBlank()) return ApiResult.error("题库名称不能为空");

        BizQuestionBank bank = new BizQuestionBank();
        bank.setBankName(bankName);
        bank.setIsPublic(isPublic);
        bank.setTeacherId(getCurrentUserId());
        LocalDateTime now = LocalDateTime.now();
        bank.setCreatedAt(now);
        bank.setUpdatedAt(now);
        bankService.save(bank);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑题库")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        BizQuestionBank bank = bankService.getById(Long.parseLong(id));
        if (bank == null) return ApiResult.error("题库不存在");
        if (!Long.valueOf(userId).equals(bank.getTeacherId())) return ApiResult.error("越权操作，您只能修改自己创建的题库");

        String bankName = (String) body.get("bankName");
        if (bankName != null && !bankName.isBlank()) bank.setBankName(bankName);
        if (body.get("isPublic") != null) bank.setIsPublic(Integer.valueOf(body.get("isPublic").toString()));
        bank.setUpdatedAt(LocalDateTime.now());
        bankService.updateById(bank);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除题库")
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> delete(@PathVariable String id) {
        Long userId = getCurrentUserId();
        Long bankId = Long.parseLong(id);
        BizQuestionBank bank = bankService.getById(bankId);
        if (bank == null) return ApiResult.error("题库不存在");
        if (!Long.valueOf(userId).equals(bank.getTeacherId())) return ApiResult.error("越权操作，您只能删除自己创建的题库");

        long count = questionService.count(new LambdaQueryWrapper<BizQuestion>().eq(BizQuestion::getBankId, bankId));
        if (count > 0) return ApiResult.error("该题库下存在题目，不可直接删除，请先清空题目");

        bankService.removeById(bankId);
        return ApiResult.ok();
    }
}