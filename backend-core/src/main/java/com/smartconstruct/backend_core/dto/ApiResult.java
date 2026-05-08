package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装
 *
 * 所有 Controller 接口的返回值统一使用此包装类
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
        return new ApiResult<>(200, "success", data);
    }

    /**
     * 成功响应（无数据）
     *
     * @return ApiResult<Void> 实例
     */
    public static ApiResult<Void> ok() {
        return new ApiResult<>(200, "success", null);
    }

    /**
     * 错误响应（自定义状态码）
     *
     * @param code    状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResult 实例
     */
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    /**
     * 错误响应（默认400状态码）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResult 实例
     */
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(400, message, null);
    }
}
