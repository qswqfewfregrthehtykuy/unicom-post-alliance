package com.unicom.post.modules.system.service.impl;

import com.unicom.post.modules.auth.domain.UserPrincipal;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.system.domain.entity.SystemOperationLog;
import com.unicom.post.modules.system.mapper.SystemOperationLogMapper;
import com.unicom.post.modules.system.service.SysOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service("operationLogService")
@Slf4j
public class SysOperationLogServiceImpl implements SysOperationLogService {

    private final SystemOperationLogMapper logMapper;
    private final SysUserService userService;

    public SysOperationLogServiceImpl(SystemOperationLogMapper logMapper, SysUserService userService) {
        this.logMapper = logMapper;
        this.userService = userService;
    }

    @Override
    public void log(String module, String action, Long targetId, String description,
                    String ipAddress, String result, String errorMsg) {
        logDetail(module, action, targetId, null, description, null, null, ipAddress, result, errorMsg);
    }

    @Override
    public void logDetail(String module, String action, Long targetId, String targetType,
                          String description, String beforeData, String afterData,
                          String ipAddress, String result, String errorMsg) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = (authentication != null) ? authentication.getName() : "SYSTEM";
            Long userId = 0L;
            String role = "UNKNOWN";

            if (username != null && !"anonymousUser".equals(username) && authentication != null) {
                // 尝试从 UserPrincipal 获取 userId 和角色（避免查库）
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserPrincipal) {
                    UserPrincipal up = (UserPrincipal) principal;
                    userId = up.getId();
                    List<String> roles = up.getRoles();
                    if (roles != null && !roles.isEmpty()) {
                        role = String.join(",", roles);
                    }
                } else {
                    // 兼容旧逻辑：查库获取
                    SysUser user = userService.findByUsername(username);
                    if (user != null) {
                        userId = user.getId();
                    }
                }
            }

            SystemOperationLog logEntity = new SystemOperationLog();
            logEntity.setUserId(userId);
            logEntity.setUsername(username);
            logEntity.setRole(role);
            logEntity.setModule(module);
            // 将 action 和 description 合并存储到 action 字段
            if (description != null && !description.isEmpty()) {
                logEntity.setAction(action + " - " + description);
            } else {
                logEntity.setAction(action);
            }
            logEntity.setTargetId(targetId);
            logEntity.setTargetType(targetType);
            logEntity.setBeforeData(beforeData);
            logEntity.setAfterData(afterData);
            logEntity.setIpAddress(ipAddress);
            logEntity.setResult(result);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setCreatedAt(LocalDateTime.now());

            logMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("记录操作日志失败：", e);
        }
    }
}