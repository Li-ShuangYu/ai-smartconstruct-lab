package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.mapper.WfNodeInstanceMapper;
import com.smartconstruct.backend_core.mapper.WfStudentNodeProgressMapper;
import com.smartconstruct.backend_core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartconstruct.backend_core.dto.DemoSeedResult;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 演示数据播种服务
 *
 * 负责生成完整的演示数据并入库，包括：
 * 1. 基于【Python算法入门】模板生成模拟AI内容
 * 2. 批量创建演示学生账号
 * 3. 创建实训任务并分配给演示班级
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Service
public class DemoDataSeederService {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeederService.class);

    private final ITrainingTemplateService trainingTemplateService;
    private final SysUserService sysUserService;
    private final IStudentService studentService;
    private final ITrainingTaskService trainingTaskService;
    private final IAdminClassService adminClassService;
    private final IMajorService majorService;
    private final ITrainingParticipationService participationService;
    private final INodeDefService nodeDefService;
    private final WfNodeInstanceMapper nodeInstanceMapper;
    private final WfStudentNodeProgressMapper nodeProgressMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DemoDataSeederService(
            ITrainingTemplateService trainingTemplateService,
            SysUserService sysUserService,
            IStudentService studentService,
            ITrainingTaskService trainingTaskService,
            IAdminClassService adminClassService,
            IMajorService majorService,
            ITrainingParticipationService participationService,
            INodeDefService nodeDefService,
            WfNodeInstanceMapper nodeInstanceMapper,
            WfStudentNodeProgressMapper nodeProgressMapper,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.trainingTemplateService = trainingTemplateService;
        this.sysUserService = sysUserService;
        this.studentService = studentService;
        this.trainingTaskService = trainingTaskService;
        this.adminClassService = adminClassService;
        this.majorService = majorService;
        this.participationService = participationService;
        this.nodeDefService = nodeDefService;
        this.nodeInstanceMapper = nodeInstanceMapper;
        this.nodeProgressMapper = nodeProgressMapper;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行完整的演示数据播种流程
     *
     * 按顺序执行：生成模拟内容 → 创建演示学生 → 创建任务并分配
     *
     * @return 播种结果统计 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public DemoSeedResult seedAll() {
        log.info("===== 开始演示数据播种 =====");

        // 查找【Python算法入门】模板
        LambdaQueryWrapper<WfTrainingTemplate> templateQuery = new LambdaQueryWrapper<>();
        templateQuery.like(WfTrainingTemplate::getTemplateName, "Python算法入门");
        WfTrainingTemplate template = trainingTemplateService.getOne(templateQuery);
        if (template == null) {
            throw new RuntimeException("目标模板【Python算法入门】不存在，无法执行演示数据播种");
        }

        // 查找"26软件工程1班"
        LambdaQueryWrapper<BizAdminClass> classQuery = new LambdaQueryWrapper<>();
        classQuery.eq(BizAdminClass::getClassName, "26软件工程1班");
        BizAdminClass adminClass = adminClassService.getOne(classQuery);
        if (adminClass == null) {
            throw new RuntimeException("目标班级【26软件工程1班】不存在，无法执行演示数据播种");
        }

        // Step 1: 生成模拟AI内容
        generateAndSeedContent(template.getId());

        // Step 2: 创建演示学生
        int studentsCreated = createDemoStudents(20);

        // Step 3: 创建实训任务并分配
        TaskAssignResult assignResult = createTaskAndAssign(template.getId(), adminClass.getId());

        DemoSeedResult result = new DemoSeedResult();
        result.setTemplateId(template.getId());
        result.setClassId(adminClass.getId());
        result.setStudentsCreated(studentsCreated);
        result.setTaskId(assignResult.getTaskId());
        result.setNodeInstanceCount(assignResult.getNodeInstanceCount());
        result.setParticipationCount(assignResult.getParticipationCount());
        result.setNodeProgressCount(assignResult.getNodeProgressCount());
        result.setMessage("演示数据播种完成");

        log.info("===== 演示数据播种完成 =====");
        return result;
    }

    /**
     * 步骤1: 生成模拟AI内容并写入模板的 standard_payload_json 字段
     *
     * 读取模板的 phases_json，遍历所有阶段中的每个节点，
     * 根据 node_type 生成对应的模拟内容数据。
     *
     * @param templateId 模板ID
     */
    public void generateAndSeedContent(Long templateId) {
        log.info("Step 1: 生成模拟AI内容, templateId={}", templateId);

        WfTrainingTemplate template = trainingTemplateService.getById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在, templateId=" + templateId);
        }

        List<JsonNode> phases = parsePhasesJson(template.getPhasesJson());
        if (phases.isEmpty() && template.getRawCanvasJson() != null) {
            log.info("phases_json 为空，从 raw_canvas_json 解析 phases");
            try {
                JsonNode canvasNode;
                if (template.getRawCanvasJson() instanceof String) {
                    canvasNode = objectMapper.readTree((String) template.getRawCanvasJson());
                } else {
                    canvasNode = objectMapper.valueToTree(template.getRawCanvasJson());
                }
                JsonNode phasesNode = canvasNode.get("phases");
                if (phasesNode != null && phasesNode.isArray()) {
                    for (JsonNode node : phasesNode) {
                        phases.add(node);
                    }
                    phases.sort(Comparator.comparingInt(n -> n.has("sort_num") ? n.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE));
                    log.info("从 raw_canvas_json 解析到 {} 个阶段", phases.size());
                }
            } catch (Exception e) {
                log.error("从 raw_canvas_json 解析 phases 失败: {}", e.getMessage());
            }
        }
        if (phases.isEmpty()) {
            throw new RuntimeException("模板 phases_json 为空或无法解析, templateId=" + templateId);
        }

        // 构建 standard_payload_json: { "node_canvas_id": { "ai_generated_content": {...} } }
        ObjectNode payloadRoot = objectMapper.createObjectNode();

        for (JsonNode phase : phases) {
            JsonNode nodesArray = phase.get("nodes");
            if (nodesArray == null || !nodesArray.isArray()) {
                continue;
            }

            for (JsonNode node : nodesArray) {
                String nodeId = node.has("id") ? node.get("id").asText() :
                        (node.has("node_id") ? node.get("node_id").asText() : null);
                String nodeType = node.has("node_type") ? node.get("node_type").asText() :
                        (node.has("nodeType") ? node.get("nodeType").asText() : "");
                String nodeName = node.has("node_name") ? node.get("node_name").asText() :
                        (node.has("nodeName") ? node.get("nodeName").asText() : "");

                if (nodeId == null || nodeId.isEmpty()) {
                    continue;
                }

                // 根据 node_type 生成对应内容
                ObjectNode contentNode = generateContentForNodeType(nodeType.toLowerCase(), nodeName);

                ObjectNode nodePayload = objectMapper.createObjectNode();
                nodePayload.set("ai_generated_content", contentNode);
                payloadRoot.set(nodeId, nodePayload);
            }
        }

        // 写入模板
        template.setStandardPayloadJson(payloadRoot);
        template.setAiStatus(2); // 已完成
        template.setUpdatedAt(LocalDateTime.now());
        trainingTemplateService.updateById(template);

        log.info("模拟AI内容生成完成，共处理 {} 个节点", payloadRoot.size());
    }

    /**
     * 根据节点类型生成对应的模拟内容
     */
    private ObjectNode generateContentForNodeType(String nodeType, String nodeName) {
        switch (nodeType) {
            case "theory_class":
                return generateTheoryClassContent(nodeName);
            case "coding_class":
                return generateCodingClassContent(nodeName);
            case "resource_read":
                return generateResourceReadContent(nodeName);
            case "learning_survey":
            case "semester_survey":
                return generateLearningSurveyContent(nodeName);
            case "homework":
                return generateHomeworkContent(nodeName);
            case "exam":
                return generateExamContent(nodeName);
            default:
                return generateDefaultContent(nodeType, nodeName);
        }
    }

    /**
     * 生成 theory_class 节点内容 — slides 数组
     * 包含 5-15 个 slide，每个含 type/title/content 及类型特定字段
     */
    private ObjectNode generateTheoryClassContent(String nodeName) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode slides = objectMapper.createArrayNode();

        // Slide 1: intro
        ObjectNode slide1 = objectMapper.createObjectNode();
        slide1.put("type", "intro");
        slide1.put("title", "Python算法入门概述");
        slide1.put("content", "本节将介绍Python算法的基本概念和常用数据结构，为后续学习打下基础。");
        ArrayNode bulletPoints1 = objectMapper.createArrayNode();
        bulletPoints1.add("算法是解决问题的步骤化方法");
        bulletPoints1.add("Python提供丰富的内置数据结构");
        bulletPoints1.add("掌握基础算法是编程进阶的关键");
        slide1.set("bullet_points", bulletPoints1);
        slides.add(slide1);

        // Slide 2: theory
        ObjectNode slide2 = objectMapper.createObjectNode();
        slide2.put("type", "theory");
        slide2.put("title", "列表与数组操作");
        slide2.put("content", "Python列表是最常用的数据结构，支持动态大小调整和多种操作方法。");
        ArrayNode bulletPoints2 = objectMapper.createArrayNode();
        bulletPoints2.add("列表使用方括号[]创建");
        bulletPoints2.add("支持索引、切片、追加、删除等操作");
        bulletPoints2.add("列表推导式提供简洁的创建方式");
        slide2.set("bullet_points", bulletPoints2);
        slides.add(slide2);

        // Slide 3: code
        ObjectNode slide3 = objectMapper.createObjectNode();
        slide3.put("type", "code");
        slide3.put("title", "列表排序示例");
        slide3.put("content", "以下代码演示了Python列表的排序操作");
        slide3.put("code", "numbers = [64, 34, 25, 12, 22, 11, 90]\n\n# 使用内置sort()方法（原地排序）\nnumbers.sort()\nprint(\"升序排列:\", numbers)\n\n# 使用sorted()函数（返回新列表）\noriginal = [64, 34, 25, 12]\nresult = sorted(original, reverse=True)\nprint(\"降序排列:\", result)");
        slide3.put("output", "升序排列: [11, 12, 22, 25, 34, 64, 90]\n降序排列: [64, 34, 25, 12]");
        slides.add(slide3);

        // Slide 4: theory
        ObjectNode slide4 = objectMapper.createObjectNode();
        slide4.put("type", "theory");
        slide4.put("title", "时间复杂度基础");
        slide4.put("content", "时间复杂度用大O表示法描述算法执行时间随输入规模增长的趋势。");
        ArrayNode bulletPoints4 = objectMapper.createArrayNode();
        bulletPoints4.add("O(1) - 常数时间：与输入无关");
        bulletPoints4.add("O(n) - 线性时间：遍历一次");
        bulletPoints4.add("O(n²) - 平方时间：嵌套循环");
        bulletPoints4.add("O(log n) - 对数时间：二分查找");
        slide4.set("bullet_points", bulletPoints4);
        slides.add(slide4);

        // Slide 5: code
        ObjectNode slide5 = objectMapper.createObjectNode();
        slide5.put("type", "code");
        slide5.put("title", "冒泡排序实现");
        slide5.put("content", "冒泡排序通过相邻元素比较和交换实现排序");
        slide5.put("code", "def bubble_sort(arr):\n    n = len(arr)\n    for i in range(n):\n        for j in range(0, n-i-1):\n            if arr[j] > arr[j+1]:\n                arr[j], arr[j+1] = arr[j+1], arr[j]\n    return arr\n\ndata = [5, 3, 8, 1, 2]\nprint(bubble_sort(data))");
        slide5.put("output", "[1, 2, 3, 5, 8]");
        slides.add(slide5);

        // Slide 6: theory
        ObjectNode slide6 = objectMapper.createObjectNode();
        slide6.put("type", "theory");
        slide6.put("title", "选择排序原理");
        slide6.put("content", "选择排序每次从未排序部分选出最小元素放到已排序部分的末尾。");
        ArrayNode bulletPoints6 = objectMapper.createArrayNode();
        bulletPoints6.add("时间复杂度：O(n²)");
        bulletPoints6.add("空间复杂度：O(1)，原地排序");
        bulletPoints6.add("不稳定排序算法");
        slide6.set("bullet_points", bulletPoints6);
        slides.add(slide6);

        // Slide 7: code
        ObjectNode slide7 = objectMapper.createObjectNode();
        slide7.put("type", "code");
        slide7.put("title", "选择排序实现");
        slide7.put("content", "以下是选择排序的Python实现");
        slide7.put("code", "def selection_sort(arr):\n    n = len(arr)\n    for i in range(n):\n        min_idx = i\n        for j in range(i+1, n):\n            if arr[j] < arr[min_idx]:\n                min_idx = j\n        arr[i], arr[min_idx] = arr[min_idx], arr[i]\n    return arr\n\ndata = [29, 10, 14, 37, 13]\nprint(selection_sort(data))");
        slide7.put("output", "[10, 13, 14, 29, 37]");
        slides.add(slide7);

        // Slide 8: quiz
        ObjectNode slide8 = objectMapper.createObjectNode();
        slide8.put("type", "quiz");
        slide8.put("title", "排序算法知识检测");
        slide8.put("content", "请回答以下关于排序算法的问题");
        ArrayNode questions = objectMapper.createArrayNode();

        ObjectNode q1 = objectMapper.createObjectNode();
        q1.put("question", "冒泡排序的最坏时间复杂度是？");
        ArrayNode opts1 = objectMapper.createArrayNode();
        opts1.add("O(n)");
        opts1.add("O(n log n)");
        opts1.add("O(n²)");
        opts1.add("O(1)");
        q1.set("options", opts1);
        q1.put("answer", "O(n²)");
        questions.add(q1);

        ObjectNode q2 = objectMapper.createObjectNode();
        q2.put("question", "以下哪个排序算法是稳定的？");
        ArrayNode opts2 = objectMapper.createArrayNode();
        opts2.add("选择排序");
        opts2.add("冒泡排序");
        opts2.add("快速排序");
        opts2.add("堆排序");
        q2.set("options", opts2);
        q2.put("answer", "冒泡排序");
        questions.add(q2);

        slide8.set("questions", questions);
        slides.add(slide8);

        // Slide 9: summary
        ObjectNode slide9 = objectMapper.createObjectNode();
        slide9.put("type", "summary");
        slide9.put("title", "本节小结");
        slide9.put("content", "本节学习了Python基础排序算法的原理和实现。");
        ArrayNode keyPoints = objectMapper.createArrayNode();
        keyPoints.add("冒泡排序：相邻比较交换，O(n²)");
        keyPoints.add("选择排序：选最小放前面，O(n²)");
        keyPoints.add("Python内置sort()使用TimSort，O(n log n)");
        keyPoints.add("理解时间复杂度有助于选择合适算法");
        slide9.set("key_points", keyPoints);
        slides.add(slide9);

        root.set("slides", slides);
        return root;
    }

    /**
     * 生成 coding_class 节点内容 — coding_task 对象
     * 包含 description/code_template/hints/test_cases/language
     */
    private ObjectNode generateCodingClassContent(String nodeName) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode codingTask = objectMapper.createObjectNode();

        codingTask.put("description", "实现二分查找算法。给定一个已排序的整数列表和目标值，返回目标值在列表中的索引。如果目标值不存在，返回-1。要求使用迭代方式实现，时间复杂度为O(log n)。");
        codingTask.put("code_template", "def binary_search(arr, target):\n    \"\"\"二分查找算法\n    \n    Args:\n        arr: 已排序的整数列表（升序）\n        target: 要查找的目标值\n    \n    Returns:\n        目标值的索引，不存在则返回-1\n    \"\"\"\n    # TODO: 在此实现二分查找\n    left = 0\n    right = len(arr) - 1\n    \n    # 请补充循环逻辑\n    pass");
        codingTask.put("language", "python");

        ArrayNode hints = objectMapper.createArrayNode();
        hints.add("二分查找的核心思想是每次将搜索范围缩小一半");
        hints.add("使用left和right两个指针维护搜索区间");
        hints.add("计算中间位置mid = (left + right) // 2");
        hints.add("比较arr[mid]与target，决定搜索左半还是右半");
        codingTask.set("hints", hints);

        ArrayNode testCases = objectMapper.createArrayNode();

        ObjectNode tc1 = objectMapper.createObjectNode();
        tc1.put("description", "目标在数组中间");
        tc1.put("input", "[1, 3, 5, 7, 9, 11], 7");
        tc1.put("expected", "3");
        testCases.add(tc1);

        ObjectNode tc2 = objectMapper.createObjectNode();
        tc2.put("description", "目标在数组开头");
        tc2.put("input", "[1, 3, 5, 7, 9], 1");
        tc2.put("expected", "0");
        testCases.add(tc2);

        ObjectNode tc3 = objectMapper.createObjectNode();
        tc3.put("description", "目标在数组末尾");
        tc3.put("input", "[2, 4, 6, 8, 10], 10");
        tc3.put("expected", "4");
        testCases.add(tc3);

        ObjectNode tc4 = objectMapper.createObjectNode();
        tc4.put("description", "目标不存在");
        tc4.put("input", "[1, 2, 3, 4, 5], 6");
        tc4.put("expected", "-1");
        testCases.add(tc4);

        ObjectNode tc5 = objectMapper.createObjectNode();
        tc5.put("description", "空数组");
        tc5.put("input", "[], 1");
        tc5.put("expected", "-1");
        testCases.add(tc5);

        ObjectNode tc6 = objectMapper.createObjectNode();
        tc6.put("description", "单元素数组命中");
        tc6.put("input", "[5], 5");
        tc6.put("expected", "0");
        testCases.add(tc6);

        codingTask.set("test_cases", testCases);
        root.set("coding_task", codingTask);
        return root;
    }

    /**
     * 生成 resource_read 节点内容 — summary + key_points
     */
    private ObjectNode generateResourceReadContent(String nodeName) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("summary", "本资源介绍了Python编程语言的基础知识，包括变量定义、数据类型、条件判断和循环结构。Python以其简洁的语法和强大的标准库成为算法学习的首选语言。");

        ArrayNode keyPoints = objectMapper.createArrayNode();
        keyPoints.add("Python使用动态类型系统，变量无需声明类型");
        keyPoints.add("基本数据类型包括int、float、str、bool、list、dict、tuple");
        keyPoints.add("缩进是Python语法的重要组成部分");
        keyPoints.add("for循环和while循环是两种基本迭代方式");
        keyPoints.add("函数使用def关键字定义，支持默认参数和可变参数");
        root.set("key_points", keyPoints);
        return root;
    }

    /**
     * 生成 learning_survey 节点内容 — questions 数组
     */
    private ObjectNode generateLearningSurveyContent(String nodeName) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode questions = objectMapper.createArrayNode();

        ObjectNode q1 = objectMapper.createObjectNode();
        q1.put("question", "你之前是否有Python编程经验？");
        q1.put("type", "single_choice");
        ArrayNode opts1 = objectMapper.createArrayNode();
        opts1.add("完全没有");
        opts1.add("学过基础语法");
        opts1.add("做过小项目");
        opts1.add("有丰富经验");
        q1.set("options", opts1);
        questions.add(q1);

        ObjectNode q2 = objectMapper.createObjectNode();
        q2.put("question", "你对以下哪些算法主题感兴趣？");
        q2.put("type", "multiple_choice");
        ArrayNode opts2 = objectMapper.createArrayNode();
        opts2.add("排序算法");
        opts2.add("搜索算法");
        opts2.add("递归与分治");
        opts2.add("动态规划");
        opts2.add("图算法");
        q2.set("options", opts2);
        questions.add(q2);

        ObjectNode q3 = objectMapper.createObjectNode();
        q3.put("question", "你期望通过本课程达到什么水平？");
        q3.put("type", "single_choice");
        ArrayNode opts3 = objectMapper.createArrayNode();
        opts3.add("理解基本算法概念");
        opts3.add("能独立实现常见算法");
        opts3.add("能分析算法复杂度并优化");
        opts3.add("能解决竞赛级别的算法题");
        q3.set("options", opts3);
        questions.add(q3);

        root.set("questions", questions);
        return root;
    }

    /**
     * 生成 homework 节点内容 — questions 数组（含 answer/score）
     */
    private ObjectNode generateHomeworkContent(String nodeName) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode questions = objectMapper.createArrayNode();

        ObjectNode q1 = objectMapper.createObjectNode();
        q1.put("question", "请简述冒泡排序的基本原理，并分析其时间复杂度。");
        ArrayNode opts1 = objectMapper.createArrayNode();
        opts1.add("通过相邻元素比较交换，每轮将最大值冒泡到末尾");
        opts1.add("每次选择最小元素放到前面");
        opts1.add("使用分治策略递归排序");
        opts1.add("使用堆结构维护有序性");
        q1.set("options", opts1);
        q1.put("answer", "通过相邻元素比较交换，每轮将最大值冒泡到末尾");
        q1.put("score", 20);
        questions.add(q1);

        ObjectNode q2 = objectMapper.createObjectNode();
        q2.put("question", "二分查找的前提条件是什么？");
        ArrayNode opts2 = objectMapper.createArrayNode();
        opts2.add("数组必须是有序的");
        opts2.add("数组元素必须是整数");
        opts2.add("数组长度必须是2的幂");
        opts2.add("数组不能有重复元素");
        q2.set("options", opts2);
        q2.put("answer", "数组必须是有序的");
        q2.put("score", 20);
        questions.add(q2);

        ObjectNode q3 = objectMapper.createObjectNode();
        q3.put("question", "以下哪个排序算法的平均时间复杂度为O(n log n)？");
        ArrayNode opts3 = objectMapper.createArrayNode();
        opts3.add("冒泡排序");
        opts3.add("选择排序");
        opts3.add("快速排序");
        opts3.add("插入排序");
        q3.set("options", opts3);
        q3.put("answer", "快速排序");
        q3.put("score", 20);
        questions.add(q3);

        ObjectNode q4 = objectMapper.createObjectNode();
        q4.put("question", "递归函数必须包含哪两个要素？");
        ArrayNode opts4 = objectMapper.createArrayNode();
        opts4.add("基准条件和递归调用");
        opts4.add("循环和条件判断");
        opts4.add("输入和输出");
        opts4.add("变量和常量");
        q4.set("options", opts4);
        q4.put("answer", "基准条件和递归调用");
        q4.put("score", 20);
        questions.add(q4);

        ObjectNode q5 = objectMapper.createObjectNode();
        q5.put("question", "Python中list.sort()和sorted()的主要区别是？");
        ArrayNode opts5 = objectMapper.createArrayNode();
        opts5.add("sort()原地排序修改原列表，sorted()返回新列表");
        opts5.add("sort()更快，sorted()更慢");
        opts5.add("sort()只能升序，sorted()可以降序");
        opts5.add("没有区别，完全相同");
        q5.set("options", opts5);
        q5.put("answer", "sort()原地排序修改原列表，sorted()返回新列表");
        q5.put("score", 20);
        questions.add(q5);

        root.set("questions", questions);
        return root;
    }

    /**
     * 生成 exam 节点内容 — questions 数组（含 answer/score）
     */
    private ObjectNode generateExamContent(String nodeName) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode questions = objectMapper.createArrayNode();

        ObjectNode q1 = objectMapper.createObjectNode();
        q1.put("question", "线性查找的时间复杂度是？");
        ArrayNode opts1 = objectMapper.createArrayNode();
        opts1.add("O(1)");
        opts1.add("O(log n)");
        opts1.add("O(n)");
        opts1.add("O(n²)");
        q1.set("options", opts1);
        q1.put("answer", "O(n)");
        q1.put("score", 10);
        questions.add(q1);

        ObjectNode q2 = objectMapper.createObjectNode();
        q2.put("question", "插入排序在最好情况下的时间复杂度是？");
        ArrayNode opts2 = objectMapper.createArrayNode();
        opts2.add("O(n²)");
        opts2.add("O(n)");
        opts2.add("O(n log n)");
        opts2.add("O(1)");
        q2.set("options", opts2);
        q2.put("answer", "O(n)");
        q2.put("score", 10);
        questions.add(q2);

        ObjectNode q3 = objectMapper.createObjectNode();
        q3.put("question", "以下哪种数据结构适合实现FIFO（先进先出）？");
        ArrayNode opts3 = objectMapper.createArrayNode();
        opts3.add("栈");
        opts3.add("队列");
        opts3.add("堆");
        opts3.add("二叉树");
        q3.set("options", opts3);
        q3.put("answer", "队列");
        q3.put("score", 10);
        questions.add(q3);

        ObjectNode q4 = objectMapper.createObjectNode();
        q4.put("question", "Python中实现递归计算阶乘，factorial(0)应返回？");
        ArrayNode opts4 = objectMapper.createArrayNode();
        opts4.add("0");
        opts4.add("1");
        opts4.add("-1");
        opts4.add("None");
        q4.set("options", opts4);
        q4.put("answer", "1");
        q4.put("score", 10);
        questions.add(q4);

        root.set("questions", questions);
        return root;
    }

    /**
     * 生成其他节点类型的默认内容 — title + content 占位
     */
    private ObjectNode generateDefaultContent(String nodeType, String nodeName) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("title", nodeName.isEmpty() ? "Python算法入门 - " + nodeType : nodeName);
        root.put("content", "本节点为" + nodeType + "类型，内容围绕Python算法入门主题。请按照指引完成相关学习任务。");
        return root;
    }

    /**
     * 步骤2: 创建演示学生账号
     *
     * 创建指定数量的演示学生用户，包含 sys_user 和 biz_student 记录。
     * 幂等性：基于 username 唯一键判断，跳过已存在记录。
     *
     * @param count 要创建的学生数量
     * @return 实际创建的学生数量
     */
    public int createDemoStudents(int count) {
        log.info("Step 2: 创建演示学生, count={}", count);

        // 查找"26软件工程1班"获取 classId, deptId, majorId
        LambdaQueryWrapper<BizAdminClass> classQuery = new LambdaQueryWrapper<>();
        classQuery.eq(BizAdminClass::getClassName, "26软件工程1班");
        BizAdminClass adminClass = adminClassService.getOne(classQuery);
        if (adminClass == null) {
            throw new RuntimeException("目标班级【26软件工程1班】不存在");
        }

        // 通过 majorId 获取 deptId
        Long majorId = adminClass.getMajorId();
        Long classId = adminClass.getId();
        BizMajor major = majorService.getById(majorId);
        Long deptId = (major != null) ? major.getDeptId() : majorId;

        int created = 0;
        int skipped = 0;

        for (int i = 1; i <= count; i++) {
            String username = String.format("demo_student_%02d", i);
            String realName = String.format("演示学生%02d", i);
            String studentNo = String.format("20260100%02d", i);

            // 幂等性检查：基于 username 判断
            LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
            userQuery.eq(SysUser::getUsername, username);
            SysUser existingUser = sysUserService.getOne(userQuery);
            if (existingUser != null) {
                skipped++;
                continue;
            }

            LocalDateTime now = LocalDateTime.now();

            // 创建 sys_user 记录
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode("123456"));
            user.setRoleType(3); // 学生
            user.setStatus(0);  // 正常
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            sysUserService.save(user);

            // 创建 biz_student 记录
            BizStudent student = new BizStudent();
            student.setUserId(user.getId());
            student.setStudentNo(studentNo);
            student.setRealName(realName);
            student.setDeptId(deptId);
            student.setMajorId(majorId);
            student.setClassId(classId);
            student.setCreatedAt(now);
            student.setUpdatedAt(now);
            studentService.save(student);

            created++;
        }

        log.info("演示学生创建完成: created={}, skipped={}", created, skipped);
        return created;
    }

    /**
     * 步骤3: 创建实训任务并分配给班级
     *
     * 基于模板创建 Training_Task，创建 Node_Instance，
     * 为班级中每个学生创建 Participation 和 Node_Progress 记录。
     *
     * @param templateId 模板ID
     * @param classId 班级ID
     * @return 任务分配结果统计
     */
    public TaskAssignResult createTaskAndAssign(Long templateId, Long classId) {
        log.info("Step 3: 创建实训任务并分配, templateId={}, classId={}", templateId, classId);

        // 1. 读取模板信息
        WfTrainingTemplate template = trainingTemplateService.getById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在, templateId=" + templateId);
        }

        // 2. 创建 Training_Task 记录
        LocalDateTime now = LocalDateTime.now();
        BizTrainingTask task = new BizTrainingTask();
        task.setTaskName(template.getTemplateName());
        task.setTemplateId(templateId);
        task.setTeacherId(template.getCreatorId());
        task.setIsInClass(0);           // 课后实训
        task.setDispatchScope(1);       // 行政班
        task.setDispatchTargetId(classId);
        task.setStatus(0);              // 未开始
        task.setStartTime(now);
        task.setEndTime(now.plusDays(7));
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        trainingTaskService.save(task);
        Long taskId = task.getId();
        log.info("创建实训任务成功, taskId={}, taskName={}", taskId, task.getTaskName());

        // 3. 解析 phases_json 和 standard_payload_json，创建 Node_Instance 记录
        List<JsonNode> phases = parsePhasesJson(template.getPhasesJson());
        if (phases.isEmpty() && template.getRawCanvasJson() != null) {
            try {
                JsonNode canvasNode;
                if (template.getRawCanvasJson() instanceof String) {
                    canvasNode = objectMapper.readTree((String) template.getRawCanvasJson());
                } else {
                    canvasNode = objectMapper.valueToTree(template.getRawCanvasJson());
                }
                JsonNode phasesNode = canvasNode.get("phases");
                if (phasesNode != null && phasesNode.isArray()) {
                    for (JsonNode node : phasesNode) {
                        phases.add(node);
                    }
                    phases.sort(Comparator.comparingInt(n ->
                            n.has("sort_num") ? n.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE));
                }
            } catch (Exception e) {
                log.error("createTaskAndAssign: 从 raw_canvas_json 解析 phases 失败: {}", e.getMessage());
            }
        }
        JsonNode payloadJson = parseStandardPayloadJson(template.getStandardPayloadJson());

        // 预加载所有 node_def，建立 nodeType -> nodeDefId 映射
        Map<String, Long> nodeTypeToDefId = buildNodeTypeToDefIdMap();

        List<WfNodeInstance> nodeInstances = new ArrayList<>();
        int sortNum = 1;

        for (JsonNode phase : phases) {
            String phaseId = phase.has("id") ? phase.get("id").asText() :
                    (phase.has("phase_id") ? phase.get("phase_id").asText() : null);

            JsonNode nodesArray = phase.get("nodes");
            if (nodesArray == null || !nodesArray.isArray()) {
                continue;
            }

            for (JsonNode node : nodesArray) {
                String nodeCanvasId = node.has("id") ? node.get("id").asText() :
                        (node.has("node_id") ? node.get("node_id").asText() : null);
                String nodeType = node.has("node_type") ? node.get("node_type").asText() :
                        (node.has("nodeType") ? node.get("nodeType").asText() : "");
                String nodeName = node.has("node_name") ? node.get("node_name").asText() :
                        (node.has("nodeName") ? node.get("nodeName").asText() : "");

                if (nodeCanvasId == null || nodeCanvasId.isEmpty()) {
                    continue;
                }

                // 从 standard_payload_json 中提取该节点的 ai_generated_content 作为 config_json
                Object configJson = null;
                if (payloadJson != null && payloadJson.has(nodeCanvasId)) {
                    JsonNode nodePayload = payloadJson.get(nodeCanvasId);
                    if (nodePayload.has("ai_generated_content")) {
                        configJson = nodePayload.get("ai_generated_content");
                    }
                }

                // 查找 node_def_id
                Long nodeDefId = nodeTypeToDefId.get(nodeType.toLowerCase());

                WfNodeInstance instance = new WfNodeInstance();
                instance.setTaskId(taskId);
                instance.setPhaseId(phaseId);
                instance.setNodeDefId(nodeDefId);
                instance.setNodeType(nodeType);
                instance.setNodeName(nodeName);
                instance.setSortNum(sortNum++);
                instance.setConfigJson(configJson);
                instance.setStatus(0); // 未开始
                instance.setCreatedAt(now);
                instance.setUpdatedAt(now);
                nodeInstanceMapper.insert(instance);
                nodeInstances.add(instance);
            }
        }
        log.info("创建 Node_Instance 完成, count={}", nodeInstances.size());

        // 4. 查询班级中的所有学生
        LambdaQueryWrapper<BizStudent> studentQuery = new LambdaQueryWrapper<>();
        studentQuery.eq(BizStudent::getClassId, classId);
        List<BizStudent> students = studentService.list(studentQuery);

        if (students == null || students.isEmpty()) {
            log.warn("班级 classId={} 中无学生记录，跳过 Participation 和 Node_Progress 创建", classId);
            return new TaskAssignResult(taskId, nodeInstances.size(), 0, 0);
        }

        // 5. 为每个学生创建 Participation 记录
        List<BizTrainingParticipation> participations = new ArrayList<>();
        for (BizStudent student : students) {
            BizTrainingParticipation participation = new BizTrainingParticipation();
            participation.setTaskId(taskId);
            participation.setStudentId(student.getUserId());
            participation.setStatus(0);         // 未开始
            participation.setTotalScore(null);
            participation.setCreatedAt(now);
            participation.setUpdatedAt(now);
            participationService.save(participation);
            participations.add(participation);
        }
        log.info("创建 Participation 完成, count={}", participations.size());

        // 6. 为每个学生的每个 Node_Instance 创建 Node_Progress 记录
        int progressCount = 0;
        for (BizTrainingParticipation participation : participations) {
            for (WfNodeInstance nodeInstance : nodeInstances) {
                WfStudentNodeProgress progress = new WfStudentNodeProgress();
                progress.setParticipationId(participation.getId());
                progress.setNodeInstanceId(nodeInstance.getId());
                progress.setStatus(0);              // 未开始
                progress.setEnteredAt(null);
                progress.setIsForcedComplete(0);
                progress.setCreatedAt(now);
                progress.setUpdatedAt(now);
                nodeProgressMapper.insert(progress);
                progressCount++;
            }
        }
        log.info("创建 Node_Progress 完成, count={}", progressCount);
        log.info("实训任务创建与分配完成: taskId={}, nodeInstances={}, participations={}, nodeProgress={}",
                taskId, nodeInstances.size(), participations.size(), progressCount);
        return new TaskAssignResult(taskId, nodeInstances.size(), participations.size(), progressCount);
    }

    /**
     * 解析 standard_payload_json 字段为 JsonNode
     */
    private JsonNode parseStandardPayloadJson(Object standardPayloadJson) {
        if (standardPayloadJson == null) {
            return null;
        }
        try {
            if (standardPayloadJson instanceof JsonNode) {
                return (JsonNode) standardPayloadJson;
            }
            if (standardPayloadJson instanceof String) {
                return objectMapper.readTree((String) standardPayloadJson);
            }
            String jsonStr = objectMapper.writeValueAsString(standardPayloadJson);
            return objectMapper.readTree(jsonStr);
        } catch (Exception e) {
            log.error("解析 standard_payload_json 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建 nodeType -> nodeDefId 的映射表
     * 从 wf_node_def 表加载所有节点定义
     */
    private Map<String, Long> buildNodeTypeToDefIdMap() {
        List<WfNodeDef> allDefs = nodeDefService.list();
        Map<String, Long> map = new HashMap<>();
        if (allDefs != null) {
            for (WfNodeDef def : allDefs) {
                if (def.getNodeType() != null) {
                    map.put(def.getNodeType().toLowerCase(), def.getId());
                }
            }
        }
        return map;
    }

    // ==================== Inner Classes ====================

    /**
     * 任务分配结果内部类，用于传递 createTaskAndAssign 的统计数据
     */
    public static class TaskAssignResult {
        private final Long taskId;
        private final int nodeInstanceCount;
        private final int participationCount;
        private final int nodeProgressCount;

        public TaskAssignResult(Long taskId, int nodeInstanceCount, int participationCount, int nodeProgressCount) {
            this.taskId = taskId;
            this.nodeInstanceCount = nodeInstanceCount;
            this.participationCount = participationCount;
            this.nodeProgressCount = nodeProgressCount;
        }

        public Long getTaskId() { return taskId; }
        public int getNodeInstanceCount() { return nodeInstanceCount; }
        public int getParticipationCount() { return participationCount; }
        public int getNodeProgressCount() { return nodeProgressCount; }
    }

    // ==================== Helper Methods ====================

    /**
     * 解析 phases_json 字段为 JsonNode 列表，并按 sort_num 升序排序。
     *
     * phases_json 可能是 String、List/Map（由JacksonTypeHandler反序列化），
     * 需要统一转换为 JsonNode 数组后排序。
     *
     * @param phasesJson phases_json 字段值
     * @return 按 sort_num 排序的阶段 JsonNode 列表，解析失败返回空列表
     */
    private List<JsonNode> parsePhasesJson(Object phasesJson) {
        if (phasesJson == null) {
            return new ArrayList<>();
        }

        try {
            JsonNode arrayNode;
            if (phasesJson instanceof String) {
                arrayNode = objectMapper.readTree((String) phasesJson);
            } else {
                String jsonStr = objectMapper.writeValueAsString(phasesJson);
                arrayNode = objectMapper.readTree(jsonStr);
            }

            if (!arrayNode.isArray()) {
                log.warn("phases_json 不是数组格式");
                return new ArrayList<>();
            }

            List<JsonNode> phases = new ArrayList<>();
            for (JsonNode node : arrayNode) {
                phases.add(node);
            }
            phases.sort(Comparator.comparingInt(n ->
                    n.has("sort_num") ? n.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE));
            return phases;
        } catch (Exception e) {
            log.error("解析 phases_json 失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
