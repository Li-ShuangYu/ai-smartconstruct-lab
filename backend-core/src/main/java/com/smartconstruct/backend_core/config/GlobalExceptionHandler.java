package com.smartconstruct.backend_core.config;

import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * 统一处理所有异常，确保返回格式一致的错误响应。
 * 支持的异常类型：
 * - 认证异常（未登录、Token过期、多端登录等）
 * - 权限异常
 * - 参数校验异常
 * - 业务异常
 * - 未知异常
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理认证异常（未登录、Token无效等）
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResult<Void>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResult.error(401, "AUTH_NOT_LOGIN", "未登录或登录已过期"));
    }

    /**
     * 处理凭证错误异常（密码错误等）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResult<Void>> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResult.error(401, "AUTH_BAD_CREDENTIALS", "用户名或密码错误"));
    }

    /**
     * 处理权限拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResult.error(403, "AUTH_FORBIDDEN", "权限不足，无法访问此资源"));
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error(400, "VALIDATION_ERROR", message));
    }

    /**
     * 处理业务异常（BusinessException）
     * 按异常中指定的HTTP状态码和错误码返回响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Object>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ApiResult<>(e.getHttpStatus(), e.getMessage(), e.getErrorCode(), e.getData()));
    }

    /**
     * 处理业务运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResult<Void>> handleRuntimeException(RuntimeException e) {
        // 判断是否为认证相关的运行时异常
        String message = e.getMessage();
        if (message != null && (message.contains("Token") || message.contains("token") || message.contains("过期"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResult.error(401, "AUTH_TOKEN_EXPIRED", "Token无效或已过期"));
        }
        // 判断是否为认证上下文异常（未登录用户访问需认证接口）
        if (e instanceof ClassCastException || e instanceof NullPointerException) {
            String errMsg = e.getMessage();
            if (errMsg != null && (errMsg.contains("SysUser") || errMsg.contains("Principal") || errMsg.contains("Authentication"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResult.error(401, "AUTH_NOT_LOGIN", "未登录或登录已过期"));
            }
        }
        // 判断是否为数据库/SQL异常（不应返回400）
        if (message != null && (message.contains("SQL") || message.contains("jdbc") || message.contains("Unknown column"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.error(500, "SYSTEM_INTERNAL_ERROR", "系统内部错误，请稍后重试"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error(400, "BUSINESS_ERROR", message));
    }

    /**
     * 处理所有其他异常（兜底）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.error(500, "SYSTEM_INTERNAL_ERROR", "系统内部错误，请稍后重试"));
    }
}