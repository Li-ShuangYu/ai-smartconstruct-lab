package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizQuestion;
import com.smartconstruct.backend_core.service.IQuestionService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/questions")
public class TeacherQuestionController {

    private final IQuestionService questionService;

    public TeacherQuestionController(IQuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ApiResult<List<BizQuestion>> list(@RequestParam String bankId) {
        Long id = Long.parseLong(bankId);
        List<BizQuestion> list = questionService.list(new LambdaQueryWrapper<BizQuestion>()
                .eq(BizQuestion::getBankId, id)
                .orderByAsc(BizQuestion::getSortNum));
        return ApiResult.ok(list);
    }

    @GetMapping("/{id}")
    public ApiResult<BizQuestion> detail(@PathVariable String id) {
        return ApiResult.ok(questionService.getById(Long.parseLong(id)));
    }

    @OperationLog(action = "新增题目")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> create(@RequestBody Map<String, Object> body) {
        Long bankId = body.get("bankId") != null ? Long.parseLong(body.get("bankId").toString()) : null;
        Integer questionType = body.get("questionType") != null ? Integer.valueOf(body.get("questionType").toString()) : null;
        String content = (String) body.get("content");
        String standardAnswer = (String) body.get("standardAnswer");
        BigDecimal defaultScore = body.get("defaultScore") != null ? new BigDecimal(body.get("defaultScore").toString()) : BigDecimal.valueOf(5);
        Integer sortNum = body.get("sortNum") != null ? Integer.valueOf(body.get("sortNum").toString()) : 0;

        if (questionType == null || content == null || content.trim().isEmpty()) {
            return ApiResult.error("参数不完整");
        }

        BizQuestion q = new BizQuestion();
        q.setBankId(bankId);
        q.setQuestionType(questionType);
        q.setContent(content);
        q.setStandardAnswer(standardAnswer);
        q.setDefaultScore(defaultScore);
        q.setSortNum(sortNum);
        q.setCreateType(2);
        LocalDateTime now = LocalDateTime.now();
        q.setCreatedAt(now);
        q.setUpdatedAt(now);
        questionService.save(q);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑题目")
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Long qId = Long.parseLong(id);
        BizQuestion q = questionService.getById(qId);
        if (q == null) return ApiResult.error("题目不存在");

        if (body.get("questionType") != null) q.setQuestionType(Integer.valueOf(body.get("questionType").toString()));
        String content = (String) body.get("content");
        if (content != null) q.setContent(content);
        String standardAnswer = (String) body.get("standardAnswer");
        if (standardAnswer != null) q.setStandardAnswer(standardAnswer);
        if (body.get("defaultScore") != null) q.setDefaultScore(new BigDecimal(body.get("defaultScore").toString()));
        if (body.get("sortNum") != null) q.setSortNum(Integer.valueOf(body.get("sortNum").toString()));
        q.setUpdatedAt(LocalDateTime.now());
        questionService.updateById(q);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除题目")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable String id) {
        questionService.removeById(Long.parseLong(id));
        return ApiResult.ok();
    }
}