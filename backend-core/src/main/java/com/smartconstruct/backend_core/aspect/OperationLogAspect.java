package com.smartconstruct.backend_core.aspect;

import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.entity.SysOperationLog;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.IOperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private final IOperationLogService operationLogService;

    public OperationLogAspect(IOperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(com.smartconstruct.backend_core.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        saveLogAsync(joinPoint);

        return result;
    }

    @Async
    public void saveLogAsync(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            OperationLog annotation = signature.getMethod().getAnnotation(OperationLog.class);

            SysOperationLog log = new SysOperationLog();
            log.setActionType(annotation.action());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof SysUser) {
                log.setUserId(((SysUser) auth.getPrincipal()).getId());
            }

            log.setIpAddress(getClientIp());
            log.setCreatedAt(LocalDateTime.now());
            operationLogService.save(log);
        } catch (Exception ignored) {
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "unknown";
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
