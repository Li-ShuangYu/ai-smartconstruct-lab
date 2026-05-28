package com.smartconstruct.backend_core.service.impl;

import com.smartconstruct.backend_core.dto.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.smartconstruct.backend_core.mapper.WfNodeConfigSchemaMapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NodeValidationServiceImpl.validatePhases().
 *
 * Tests cover:
 * - Valid phase arrays (1 phase, 10 phases, boundary names)
 * - Invalid JSON input
 * - Non-array JSON
 * - Array length violations (0 phases, 11+ phases)
 * - Phase name length violations (empty, >20 chars)
 * - Missing phase_name field
 * - Missing sort_num field
 * - Duplicate sort_num values
 */
@ExtendWith(MockitoExtension.class)
class NodeValidationServiceImplValidatePhasesTest {

    @Mock
    private WfNodeConfigSchemaMapper schemaMapper;

    @InjectMocks
    private NodeValidationServiceImpl validationService;

    @Test
    @DisplayName("Valid: single phase with valid name and sort_num")
    void testValidSinglePhase() {
        String json = "[{\"phase_name\": \"课前阶段\", \"sort_num\": 1}]";
        ValidationResult result = validationService.validatePhases(json);
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Valid: 10 phases (maximum allowed)")
    void testValid10Phases() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 1; i <= 10; i++) {
            if (i > 1) sb.append(",");
            sb.append("{\"phase_name\": \"Phase").append(i).append("\", \"sort_num\": ").append(i).append("}");
        }
        sb.append("]");
        ValidationResult result = validationService.validatePhases(sb.toString());
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Valid: phase name exactly 1 character")
    void testValidPhaseNameMinLength() {
        String json = "[{\"phase_name\": \"A\", \"sort_num\": 1}]";
        ValidationResult result = validationService.validatePhases(json);
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Valid: phase name exactly 20 characters")
    void testValidPhaseNameMaxLength() {
        String json = "[{\"phase_name\": \"12345678901234567890\", \"sort_num\": 1}]";
        ValidationResult result = validationService.validatePhases(json);
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Invalid: malformed JSON")
    void testInvalidJson() {
        String json = "not valid json";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("阶段JSON格式无效"));
    }

    @Test
    @DisplayName("Invalid: JSON is an object, not an array")
    void testNonArrayJson() {
        String json = "{\"phase_name\": \"test\"}";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("必须是一个JSON数组"));
    }

    @Test
    @DisplayName("Invalid: empty array (0 phases)")
    void testEmptyArray() {
        String json = "[]";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("1到10之间"));
    }

    @Test
    @DisplayName("Invalid: 11 phases (exceeds maximum)")
    void testTooManyPhases() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 1; i <= 11; i++) {
            if (i > 1) sb.append(",");
            sb.append("{\"phase_name\": \"P").append(i).append("\", \"sort_num\": ").append(i).append("}");
        }
        sb.append("]");
        ValidationResult result = validationService.validatePhases(sb.toString());
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("1到10之间"));
    }

    @Test
    @DisplayName("Invalid: phase_name is empty string")
    void testEmptyPhaseName() {
        String json = "[{\"phase_name\": \"\", \"sort_num\": 1}]";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("phase_name长度必须在1到20"));
    }

    @Test
    @DisplayName("Invalid: phase_name exceeds 20 characters")
    void testPhaseNameTooLong() {
        String json = "[{\"phase_name\": \"123456789012345678901\", \"sort_num\": 1}]";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("phase_name长度必须在1到20"));
    }

    @Test
    @DisplayName("Invalid: missing phase_name field")
    void testMissingPhaseName() {
        String json = "[{\"sort_num\": 1}]";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("缺少有效的phase_name"));
    }

    @Test
    @DisplayName("Invalid: missing sort_num field")
    void testMissingSortNum() {
        String json = "[{\"phase_name\": \"课前\"}]";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("缺少有效的sort_num"));
    }

    @Test
    @DisplayName("Invalid: duplicate sort_num values")
    void testDuplicateSortNum() {
        String json = "[{\"phase_name\": \"课前\", \"sort_num\": 1}, {\"phase_name\": \"课中\", \"sort_num\": 1}]";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("sort_num值必须唯一"));
    }

    @Test
    @DisplayName("Valid: phases with non-sequential sort_num values")
    void testNonSequentialSortNums() {
        String json = "[{\"phase_name\": \"课前\", \"sort_num\": 5}, {\"phase_name\": \"课中\", \"sort_num\": 10}]";
        ValidationResult result = validationService.validatePhases(json);
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Invalid: phase_name is not a string (number)")
    void testPhaseNameNotString() {
        String json = "[{\"phase_name\": 123, \"sort_num\": 1}]";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("缺少有效的phase_name"));
    }

    @Test
    @DisplayName("Invalid: sort_num is not a number (string)")
    void testSortNumNotNumber() {
        String json = "[{\"phase_name\": \"课前\", \"sort_num\": \"abc\"}]";
        ValidationResult result = validationService.validatePhases(json);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("缺少有效的sort_num"));
    }
}
