package com.smartconstruct.backend_core.exception;

/**
 * 业务异常
 *
 * 用于在Service层抛出可预期的业务错误，由全局异常处理器捕获并返回对应HTTP状态码。
 * 支持自定义HTTP状态码、业务错误码和附加数据。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
public class BusinessException extends RuntimeException {

    /** HTTP状态码 */
    private final int httpStatus;

    /** 业务错误码（如 PHASE_LOCKED, TASK_EXPIRED） */
    private final String errorCode;

    /** 附加数据（可选，如 current_phase_id） */
    private final Object data;

    public BusinessException(int httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.data = null;
    }

    public BusinessException(int httpStatus, String errorCode, String message, Object data) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.data = data;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getData() {
        return data;
    }
}
