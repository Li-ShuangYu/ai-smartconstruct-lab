package com.smartconstruct.backend_core.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 节点配置验证结果 DTO
 *
 * 用于 INodeValidationService 返回验证结果，包含字段级错误路径和原因。
 * 
 * 错误响应格式示例：
 * {
 *   "error_code": "VALIDATION_FAILED",
 *   "fields": [{"path": "display.welcome_text", "reason": "必填字段为空"}]
 * }
 *
 * @author SmartConstruct
 */
@Data
public class ValidationResult {

    /** 验证是否通过 */
    private boolean valid;

    /** 错误码，如 VALIDATION_FAILED、SCHEMA_NOT_FOUND */
    private String errorCode;

    /** 通用错误消息 */
    private String message;

    /** 字段级错误列表 */
    private List<FieldError> fields;

    /**
     * 字段级错误详情
     */
    @Data
    public static class FieldError {
        /** 字段路径，如 "display.welcome_text" */
        private String path;
        /** 错误原因，如 "必填字段为空" */
        private String reason;

        public FieldError() {
        }

        public FieldError(String path, String reason) {
            this.path = path;
            this.reason = reason;
        }
    }

    // ========== 静态工厂方法 ==========

    /**
     * 验证通过
     */
    public static ValidationResult success() {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        result.setFields(Collections.emptyList());
        return result;
    }

    /**
     * 验证失败（通用错误消息）
     *
     * @param message 错误消息
     */
    public static ValidationResult error(String message) {
        ValidationResult result = new ValidationResult();
        result.setValid(false);
        result.setErrorCode("VALIDATION_FAILED");
        result.setMessage(message);
        result.setFields(Collections.emptyList());
        return result;
    }

    /**
     * 验证失败（带错误码和消息）
     *
     * @param errorCode 错误码
     * @param message   错误消息
     */
    public static ValidationResult error(String errorCode, String message) {
        ValidationResult result = new ValidationResult();
        result.setValid(false);
        result.setErrorCode(errorCode);
        result.setMessage(message);
        result.setFields(Collections.<FieldError>emptyList());
        return result;
    }

    /**
     * 验证失败（带错误码和字段级错误）
     *
     * @param errorCode 错误码
     * @param fields    字段级错误列表
     */
    public static ValidationResult error(String errorCode, List<FieldError> fields) {
        ValidationResult result = new ValidationResult();
        result.setValid(false);
        result.setErrorCode(errorCode);
        result.setFields(fields != null ? fields : Collections.<FieldError>emptyList());
        return result;
    }
}
