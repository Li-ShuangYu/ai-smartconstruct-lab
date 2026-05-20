package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装
 *
 * 所有 Controller 接口的返回值统一使用此包装类
 * 
 * 扩展字段说明：
 * - errorCode: 业务错误标识，用于前端精准判断错误类型
 *   - AUTH_NOT_LOGIN: 未登录
 *   - AUTH_LOGIN_EXPIRED: 登录过期
 *   - AUTH_MULTI_LOGIN: 多端登录
 *   - AUTH_BAD_CREDENTIALS: 凭证错误
 *   - AUTH_FORBIDDEN: 权限不足
 *   - VALIDATION_ERROR: 参数校验错误
 *   - BUSINESS_ERROR: 业务逻辑错误
 *   - SYSTEM_INTERNAL_ERROR: 系统内部错误
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    /** 状态码：200=成功，400=业务错误，401=未认证，500=服务器错误 */
    private int code;
    /** 响应消息 */
    private String message;
    /** 业务错误标识，用于前端精准判断错误类型 */
    private String errorCode;
    /** 响应数据 */
    private T data;

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return ApiResult 实例
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(200, "success", null, data);
    }

    /**
     * 成功响应（无数据）
     *
     * @return ApiResult<Void> 实例
     */
    public static ApiResult<Void> ok() {
        return new ApiResult<>(200, "success", null, null);
    }

    /**
     * 错误响应（自定义状态码和错误码）
     *
     * @param code     状态码
     * @param errorCode 业务错误标识
     * @param message  错误消息
     * @param <T>      数据类型
     * @return ApiResult 实例
     */
    public static <T> ApiResult<T> error(int code, String errorCode, String message) {
        return new ApiResult<>(code, message, errorCode, null);
    }

    /**
     * 错误响应（自定义状态码，无错误码）
     *
     * @param code    状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResult 实例
     */
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null, null);
    }

    /**
     * 错误响应（默认400状态码，无错误码）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResult 实例
     */
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(400, message, "BUSINESS_ERROR", null);
    }
}
