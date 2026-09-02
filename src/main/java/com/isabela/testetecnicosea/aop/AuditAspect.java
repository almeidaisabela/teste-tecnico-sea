package com.isabela.testetecnicosea.aop;

import com.isabela.testetecnicosea.model.dto.UserResponseDTO;
import com.isabela.testetecnicosea.model.entity.AuditLog;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.time.LocalDateTime;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        long start = System.currentTimeMillis();
        Integer entityId = extractPathVariableId(joinPoint);
        User user = extractAuthenticatedUser();

        try {
            Object result = joinPoint.proceed();
            if (entityId == null) {
                entityId = extractIdFromResult(result);
            }
            saveLog(audit.action(), user, entityId, start, true, null);
            return result;
        } catch (Throwable ex) {
            saveLog(audit.action(), user, entityId, start, false, ex.getMessage());
            throw ex;
        }
    }


    private void saveLog(String action, User user, Integer entityId, long start, boolean success, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(user != null ? user.getId() : null);
            auditLog.setRole(user != null ? user.getRole() : null);
            auditLog.setAction(action);
            auditLog.setEntityId(entityId);
            auditLog.setDurationMs(System.currentTimeMillis() - start);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } catch (Exception persistException) {
            Integer userId = user != null ? user.getId() : null;
            log.error(
                    "Falha ao persistir audit log: action={} userId={}",
                    action,
                    userId,
                    persistException
            );
        }
    }


    private Integer extractPathVariableId(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals("id") && args[i] instanceof Integer) {
                return (Integer) args[i];
            }
        }
        return null;
    }


    private User extractAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Object principal = authentication != null ? authentication.getPrincipal() : null;
        return principal instanceof User ? (User) principal : null;
    }


    private Integer extractIdFromResult(Object result) {
        if (result instanceof ResponseEntity<?> response && response.getBody() instanceof UserResponseDTO userResponseDTO) {
            return userResponseDTO.id().intValue();
        }
        return null;
    }

}
