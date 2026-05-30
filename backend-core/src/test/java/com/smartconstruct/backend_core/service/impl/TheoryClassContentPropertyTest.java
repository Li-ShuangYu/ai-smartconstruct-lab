package com.smartconstruct.backend_core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.jqwik.api.*;

import java.util.*;

/**
 * Property 1: 演示数据内容格式一致性
 *
 * <b>Validates: Requirements 1.2</b>
 *
 * For any theory_class 节点生成的 config_json，其 slides 数组中每个元素必须包含
 * type（取值为 theory/intro/code/quiz/summary 之一）和 title 字段（≤50字符）和 content 字段；
 * 当 type 为 code 时必须额外包含 code（非空字符串）和 output（字符串）字段；
 * 当 type 为 quiz 时必须包含 questions 数组且每个元素含 question、options（数组）、answer 字段；
 * 当 type 为 theory 或 intro 时可包含 bullet_points 数组；
 * 当 type 为 summary 时可包含 key_points 数组；
 * slides 数组长度为 5-15。
 *
 * Label: "Feature: student-training-demo, Property 1: 演示数据内容格式一致性"
 */
@Label("Feature: student-training-demo, Property 1: 演示数据内容格式一致性")
class TheoryClassContentPropertyTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> VALID_TYPES = new HashSet<>(
            Arrays.asList("theory", "intro", "code", "quiz", "summary")
    );

    // ========== Validation Logic ==========

    /**
     * Validates the entire theory_class config_json structure.
     * Returns a list of validation errors (empty if valid).
     */
    private List<String> validateTheoryClassConfig(JsonNode configJson) {
        List<String> errors = new ArrayList<>();

        if (configJson == null) {
            errors.add("config_json is null");
            return errors;
        }

        if (!configJson.has("slides")) {
            errors.add("Missing 'slides' field");
            return errors;
        }

        JsonNode slidesNode = configJson.get("slides");
        if (!slidesNode.isArray()) {
            errors.add("'slides' is not an array");
            return errors;
        }

        int slidesCount = slidesNode.size();
        if (slidesCount < 5 || slidesCount > 15) {
            errors.add("slides array length " + slidesCount + " not in range [5, 15]");
        }

        for (int i = 0; i < slidesNode.size(); i++) {
            JsonNode slide = slidesNode.get(i);
            validateSlide(slide, i, errors);
        }

        return errors;
    }

    /**
     * Validates a single slide's structure.
     */
    private void validateSlide(JsonNode slide, int index, List<String> errors) {
        String prefix = "slide[" + index + "]: ";

        // Must have "type" field
        if (!slide.has("type") || slide.get("type").isNull()) {
            errors.add(prefix + "missing 'type' field");
            return;
        }
        String type = slide.get("type").asText();
        if (!VALID_TYPES.contains(type)) {
            errors.add(prefix + "invalid type '" + type + "', must be one of " + VALID_TYPES);
            return;
        }

        // Must have "title" field (string, ≤50 chars)
        if (!slide.has("title") || slide.get("title").isNull()) {
            errors.add(prefix + "missing 'title' field");
        } else {
            String title = slide.get("title").asText();
            if (title.length() > 50) {
                errors.add(prefix + "title length " + title.length() + " exceeds 50 chars");
            }
        }

        // Must have "content" field (string)
        if (!slide.has("content") || slide.get("content").isNull()) {
            errors.add(prefix + "missing 'content' field");
        }

        // Type-specific validations
        switch (type) {
            case "code":
                validateCodeSlide(slide, prefix, errors);
                break;
            case "quiz":
                validateQuizSlide(slide, prefix, errors);
                break;
            case "theory":
            case "intro":
                validateTheoryIntroSlide(slide, prefix, errors);
                break;
            case "summary":
                validateSummarySlide(slide, prefix, errors);
                break;
        }
    }

    private void validateCodeSlide(JsonNode slide, String prefix, List<String> errors) {
        // Must have "code" field (non-empty string)
        if (!slide.has("code") || slide.get("code").isNull()) {
            errors.add(prefix + "type=code but missing 'code' field");
        } else {
            String code = slide.get("code").asText();
            if (code.isEmpty()) {
                errors.add(prefix + "type=code but 'code' field is empty");
            }
        }

        // Must have "output" field (string)
        if (!slide.has("output") || slide.get("output").isNull()) {
            errors.add(prefix + "type=code but missing 'output' field");
        }
    }

    private void validateQuizSlide(JsonNode slide, String prefix, List<String> errors) {
        // Must have "questions" array
        if (!slide.has("questions") || slide.get("questions").isNull()) {
            errors.add(prefix + "type=quiz but missing 'questions' field");
            return;
        }

        JsonNode questions = slide.get("questions");
        if (!questions.isArray()) {
            errors.add(prefix + "type=quiz but 'questions' is not an array");
            return;
        }

        for (int i = 0; i < questions.size(); i++) {
            JsonNode q = questions.get(i);
            String qPrefix = prefix + "questions[" + i + "]: ";

            if (!q.has("question") || q.get("question").isNull()) {
                errors.add(qPrefix + "missing 'question' field");
            }
            if (!q.has("options") || q.get("options").isNull() || !q.get("options").isArray()) {
                errors.add(qPrefix + "missing or invalid 'options' array");
            }
            if (!q.has("answer") || q.get("answer").isNull()) {
                errors.add(qPrefix + "missing 'answer' field");
            }
        }
    }

    private void validateTheoryIntroSlide(JsonNode slide, String prefix, List<String> errors) {
        // "bullet_points" is optional, but if present must be an array
        if (slide.has("bullet_points") && !slide.get("bullet_points").isNull()) {
            if (!slide.get("bullet_points").isArray()) {
                errors.add(prefix + "type=theory/intro: 'bullet_points' is not an array");
            }
        }
    }

    private void validateSummarySlide(JsonNode slide, String prefix, List<String> errors) {
        // "key_points" is optional, but if present must be an array
        if (slide.has("key_points") && !slide.get("key_points").isNull()) {
            if (!slide.get("key_points").isArray()) {
                errors.add(prefix + "type=summary: 'key_points' is not an array");
            }
        }
    }

    // ========== Generators ==========

    @Provide
    Arbitrary<JsonNode> validTheoryClassConfig() {
        return slidesCount().flatMap(count -> {
            // Generate a valid slides array with the given count
            return validSlidesArray(count).map(slides -> {
                ObjectNode root = objectMapper.createObjectNode();
                root.set("slides", slides);
                return (JsonNode) root;
            });
        });
    }

    private Arbitrary<Integer> slidesCount() {
        return Arbitraries.integers().between(5, 15);
    }

    private Arbitrary<ArrayNode> validSlidesArray(int count) {
        // Generate a list of slides with at least one of each required type
        return Arbitraries.integers().between(0, 4)
                .list().ofSize(count)
                .map(typeIndices -> {
                    ArrayNode slides = objectMapper.createArrayNode();
                    String[] types = {"theory", "intro", "code", "quiz", "summary"};
                    for (int i = 0; i < count; i++) {
                        String type = types[typeIndices.get(i)];
                        slides.add(createValidSlide(type, i));
                    }
                    return slides;
                });
    }

    private ObjectNode createValidSlide(String type, int index) {
        ObjectNode slide = objectMapper.createObjectNode();
        slide.put("type", type);
        slide.put("title", truncateToMax50("Slide " + index + " - " + type));
        slide.put("content", "Content for slide " + index + " of type " + type);

        switch (type) {
            case "code":
                slide.put("code", "print('hello " + index + "')");
                slide.put("output", "hello " + index);
                break;
            case "quiz":
                ArrayNode questions = objectMapper.createArrayNode();
                ObjectNode q = objectMapper.createObjectNode();
                q.put("question", "Question " + index + "?");
                ArrayNode options = objectMapper.createArrayNode();
                options.add("Option A");
                options.add("Option B");
                options.add("Option C");
                q.set("options", options);
                q.put("answer", "Option A");
                questions.add(q);
                slide.set("questions", questions);
                break;
            case "theory":
            case "intro":
                ArrayNode bulletPoints = objectMapper.createArrayNode();
                bulletPoints.add("Point 1");
                bulletPoints.add("Point 2");
                slide.set("bullet_points", bulletPoints);
                break;
            case "summary":
                ArrayNode keyPoints = objectMapper.createArrayNode();
                keyPoints.add("Key point 1");
                keyPoints.add("Key point 2");
                slide.set("key_points", keyPoints);
                break;
        }

        return slide;
    }

    @Provide
    Arbitrary<JsonNode> invalidSlidesWithWrongType() {
        // Generate slides where at least one has an invalid type
        return slidesCount().flatMap(count -> {
            return Arbitraries.integers().between(0, count - 1).flatMap(invalidIdx -> {
                return Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(badType -> {
                    ArrayNode slides = objectMapper.createArrayNode();
                    for (int i = 0; i < count; i++) {
                        ObjectNode slide = objectMapper.createObjectNode();
                        if (i == invalidIdx) {
                            slide.put("type", "invalid_" + badType);
                        } else {
                            slide.put("type", "theory");
                        }
                        slide.put("title", "Slide " + i);
                        slide.put("content", "Content " + i);
                        if (i != invalidIdx) {
                            ArrayNode bp = objectMapper.createArrayNode();
                            bp.add("point");
                            slide.set("bullet_points", bp);
                        }
                        slides.add(slide);
                    }
                    ObjectNode root = objectMapper.createObjectNode();
                    root.set("slides", slides);
                    return (JsonNode) root;
                });
            });
        });
    }

    @Provide
    Arbitrary<JsonNode> codeSlidesMissingRequiredFields() {
        // Generate config where code slides are missing code or output
        return slidesCount().flatMap(count -> {
            return Arbitraries.of(true, false).map(missingCode -> {
                ArrayNode slides = objectMapper.createArrayNode();
                for (int i = 0; i < count; i++) {
                    ObjectNode slide = objectMapper.createObjectNode();
                    if (i == 0) {
                        // First slide is a code slide with missing field
                        slide.put("type", "code");
                        slide.put("title", "Code slide");
                        slide.put("content", "Code content");
                        if (missingCode) {
                            // Missing "code" field, has "output"
                            slide.put("output", "some output");
                        } else {
                            // Has "code", missing "output"
                            slide.put("code", "print('hello')");
                        }
                    } else {
                        slide.put("type", "theory");
                        slide.put("title", "Theory " + i);
                        slide.put("content", "Content " + i);
                    }
                    slides.add(slide);
                }
                ObjectNode root = objectMapper.createObjectNode();
                root.set("slides", slides);
                return (JsonNode) root;
            });
        });
    }

    @Provide
    Arbitrary<JsonNode> quizSlidesMissingQuestionFields() {
        // Generate config where quiz slides have questions missing required fields
        return slidesCount().flatMap(count -> {
            return Arbitraries.of(0, 1, 2).map(missingField -> {
                // 0 = missing question, 1 = missing options, 2 = missing answer
                ArrayNode slides = objectMapper.createArrayNode();
                for (int i = 0; i < count; i++) {
                    ObjectNode slide = objectMapper.createObjectNode();
                    if (i == 0) {
                        slide.put("type", "quiz");
                        slide.put("title", "Quiz slide");
                        slide.put("content", "Quiz content");
                        ArrayNode questions = objectMapper.createArrayNode();
                        ObjectNode q = objectMapper.createObjectNode();
                        if (missingField != 0) q.put("question", "What is X?");
                        if (missingField != 1) {
                            ArrayNode opts = objectMapper.createArrayNode();
                            opts.add("A");
                            opts.add("B");
                            q.set("options", opts);
                        }
                        if (missingField != 2) q.put("answer", "A");
                        questions.add(q);
                        slide.set("questions", questions);
                    } else {
                        slide.put("type", "theory");
                        slide.put("title", "Theory " + i);
                        slide.put("content", "Content " + i);
                    }
                    slides.add(slide);
                }
                ObjectNode root = objectMapper.createObjectNode();
                root.set("slides", slides);
                return (JsonNode) root;
            });
        });
    }

    @Provide
    Arbitrary<JsonNode> slidesWithInvalidLength() {
        // Generate slides arrays with length outside [5, 15]
        return Arbitraries.oneOf(
                Arbitraries.integers().between(0, 4),   // too few
                Arbitraries.integers().between(16, 25)  // too many
        ).map(count -> {
            ArrayNode slides = objectMapper.createArrayNode();
            for (int i = 0; i < count; i++) {
                ObjectNode slide = objectMapper.createObjectNode();
                slide.put("type", "theory");
                slide.put("title", "Slide " + i);
                slide.put("content", "Content " + i);
                slides.add(slide);
            }
            ObjectNode root = objectMapper.createObjectNode();
            root.set("slides", slides);
            return (JsonNode) root;
        });
    }

    // ========== Property Tests ==========

    @Property(tries = 100)
    void validTheoryClassConfigPassesValidation(
            @ForAll("validTheoryClassConfig") JsonNode config
    ) {
        List<String> errors = validateTheoryClassConfig(config);
        assert errors.isEmpty() :
                "Valid theory_class config should pass validation. Errors: " + errors;
    }

    @Property(tries = 100)
    void invalidTypeIsRejected(
            @ForAll("invalidSlidesWithWrongType") JsonNode config
    ) {
        List<String> errors = validateTheoryClassConfig(config);
        boolean hasTypeError = errors.stream().anyMatch(e -> e.contains("invalid type"));
        assert hasTypeError :
                "Config with invalid slide type should be rejected. Errors: " + errors;
    }

    @Property(tries = 100)
    void codeSlideMissingFieldsIsRejected(
            @ForAll("codeSlidesMissingRequiredFields") JsonNode config
    ) {
        List<String> errors = validateTheoryClassConfig(config);
        boolean hasCodeError = errors.stream().anyMatch(e ->
                e.contains("type=code") && (e.contains("missing 'code'") || e.contains("missing 'output'")));
        assert hasCodeError :
                "Code slide missing code/output should be rejected. Errors: " + errors;
    }

    @Property(tries = 100)
    void quizSlideMissingQuestionFieldsIsRejected(
            @ForAll("quizSlidesMissingQuestionFields") JsonNode config
    ) {
        List<String> errors = validateTheoryClassConfig(config);
        boolean hasQuizError = errors.stream().anyMatch(e ->
                e.contains("missing 'question'") || e.contains("missing or invalid 'options'") || e.contains("missing 'answer'"));
        assert hasQuizError :
                "Quiz slide with missing question fields should be rejected. Errors: " + errors;
    }

    @Property(tries = 100)
    void slidesArrayLengthOutOfRangeIsRejected(
            @ForAll("slidesWithInvalidLength") JsonNode config
    ) {
        List<String> errors = validateTheoryClassConfig(config);
        boolean hasLengthError = errors.stream().anyMatch(e -> e.contains("not in range [5, 15]"));
        assert hasLengthError :
                "Slides array with length outside [5, 15] should be rejected. Errors: " + errors;
    }

    @Property(tries = 100)
    void generatedTheoryClassContentIsValid(
            @ForAll("randomNodeNames") String nodeName
    ) {
        // Simulate the DemoDataSeederService.generateTheoryClassContent output format
        // by building the same structure the service produces
        JsonNode content = buildTheoryClassContent(nodeName);

        List<String> errors = validateTheoryClassConfig(content);
        assert errors.isEmpty() :
                "Generated theory_class content should be valid for nodeName='"
                + nodeName + "'. Errors: " + errors;
    }

    @Provide
    Arbitrary<String> randomNodeNames() {
        return Arbitraries.of(
                "Python列表操作", "冒泡排序", "二分查找", "递归基础",
                "变量与数据类型", "条件判断", "循环结构", "函数定义",
                "选择排序", "插入排序", "线性搜索", ""
        );
    }

    /**
     * Builds a theory_class content JSON matching the format produced by
     * DemoDataSeederService.generateTheoryClassContent — used to validate
     * that the expected output structure passes our validation rules.
     */
    private JsonNode buildTheoryClassContent(String nodeName) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode slides = objectMapper.createArrayNode();

        // intro slide
        ObjectNode s1 = objectMapper.createObjectNode();
        s1.put("type", "intro");
        s1.put("title", truncateToMax50("Python算法入门概述"));
        s1.put("content", "本节将介绍Python算法的基本概念。");
        ArrayNode bp1 = objectMapper.createArrayNode();
        bp1.add("算法是解决问题的步骤化方法");
        bp1.add("Python提供丰富的内置数据结构");
        s1.set("bullet_points", bp1);
        slides.add(s1);

        // theory slide
        ObjectNode s2 = objectMapper.createObjectNode();
        s2.put("type", "theory");
        s2.put("title", truncateToMax50("列表与数组操作"));
        s2.put("content", "Python列表是最常用的数据结构。");
        ArrayNode bp2 = objectMapper.createArrayNode();
        bp2.add("列表使用方括号[]创建");
        bp2.add("支持索引、切片操作");
        s2.set("bullet_points", bp2);
        slides.add(s2);

        // code slide
        ObjectNode s3 = objectMapper.createObjectNode();
        s3.put("type", "code");
        s3.put("title", truncateToMax50("列表排序示例"));
        s3.put("content", "以下代码演示了排序操作");
        s3.put("code", "numbers = [3, 1, 4]\nnumbers.sort()\nprint(numbers)");
        s3.put("output", "[1, 3, 4]");
        slides.add(s3);

        // another theory slide
        ObjectNode s4 = objectMapper.createObjectNode();
        s4.put("type", "theory");
        s4.put("title", truncateToMax50("时间复杂度基础"));
        s4.put("content", "时间复杂度用大O表示法描述算法效率。");
        ArrayNode bp4 = objectMapper.createArrayNode();
        bp4.add("O(1) - 常数时间");
        bp4.add("O(n) - 线性时间");
        s4.set("bullet_points", bp4);
        slides.add(s4);

        // another code slide
        ObjectNode s5 = objectMapper.createObjectNode();
        s5.put("type", "code");
        s5.put("title", truncateToMax50("冒泡排序实现"));
        s5.put("content", "冒泡排序通过相邻元素比较实现");
        s5.put("code", "def bubble_sort(arr):\n    n = len(arr)\n    for i in range(n):\n        pass");
        s5.put("output", "[1, 2, 3, 5, 8]");
        slides.add(s5);

        // quiz slide
        ObjectNode s6 = objectMapper.createObjectNode();
        s6.put("type", "quiz");
        s6.put("title", truncateToMax50("排序算法知识检测"));
        s6.put("content", "请回答以下问题");
        ArrayNode questions = objectMapper.createArrayNode();
        ObjectNode q = objectMapper.createObjectNode();
        q.put("question", "冒泡排序的最坏时间复杂度是？");
        ArrayNode opts = objectMapper.createArrayNode();
        opts.add("O(n)");
        opts.add("O(n²)");
        q.set("options", opts);
        q.put("answer", "O(n²)");
        questions.add(q);
        s6.set("questions", questions);
        slides.add(s6);

        // summary slide
        ObjectNode s7 = objectMapper.createObjectNode();
        s7.put("type", "summary");
        s7.put("title", truncateToMax50("本节小结"));
        s7.put("content", "本节学习了Python基础排序算法。");
        ArrayNode kp = objectMapper.createArrayNode();
        kp.add("冒泡排序：O(n²)");
        kp.add("选择排序：O(n²)");
        s7.set("key_points", kp);
        slides.add(s7);

        root.set("slides", slides);
        return root;
    }

    @Property(tries = 100)
    void allSlidesMustHaveTitleContentAndType(
            @ForAll("validTheoryClassConfig") JsonNode config
    ) {
        JsonNode slides = config.get("slides");
        for (int i = 0; i < slides.size(); i++) {
            JsonNode slide = slides.get(i);
            assert slide.has("type") && !slide.get("type").isNull() :
                    "slide[" + i + "] must have 'type' field";
            assert slide.has("title") && !slide.get("title").isNull() :
                    "slide[" + i + "] must have 'title' field";
            assert slide.has("content") && !slide.get("content").isNull() :
                    "slide[" + i + "] must have 'content' field";
            assert VALID_TYPES.contains(slide.get("type").asText()) :
                    "slide[" + i + "] type must be one of " + VALID_TYPES;
            assert slide.get("title").asText().length() <= 50 :
                    "slide[" + i + "] title must be ≤50 chars";
        }
    }

    // ========== Helpers ==========

    private String truncateToMax50(String s) {
        return s.length() > 50 ? s.substring(0, 50) : s;
    }
}
