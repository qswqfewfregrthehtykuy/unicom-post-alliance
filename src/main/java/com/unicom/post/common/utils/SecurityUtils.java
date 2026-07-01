package com.unicom.post.common.utils;

import com.unicom.post.modules.auth.domain.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }
        throw new RuntimeException("无法获取当前用户ID");
    }

    /**
     * 获取当前登录用户的第一个角色（用于权限判断）
     */
    public static String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            List<String> roles = ((UserPrincipal) principal).getRoles();
            if (roles != null && !roles.isEmpty()) {
                return roles.get(0); // 取第一个，实际可根据需要调整
            }
        }
        throw new RuntimeException("无法获取当前用户角色");
    }

    /**
     * 获取当前用户所有角色
     */
    public static List<String> getCurrentRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getRoles();
        }
        throw new RuntimeException("无法获取当前用户角色");
    }


    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

}