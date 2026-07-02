package com.unicom.post.modules.system.service;

/**
 * 操作日志记录服务
 */
public interface SysOperationLogService {

    /**
     * 记录操作日志（简单版本）
     * @param module     模块名称，如 LOGIN, USER, OUTLET 等
     * @param action     操作动作，如 "用户登录", "修改密码" 等
     * @param targetId   操作目标ID（如用户ID、订单ID等），可为null
     * @param description 操作描述
     * @param ipAddress  客户端IP
     * @param result     操作结果：SUCCESS / FAIL
     * @param errorMsg   错误信息（成功时为null）
     */
    void log(String module, String action, Long targetId, String description,
             String ipAddress, String result, String errorMsg);

    /**
     * 记录操作日志（完整版本，支持修改前后数据对比）
     * @param module      模块名称
     * @param action      操作动作
     * @param targetId    操作目标ID
     * @param targetType  目标类型，如 User, Outlet, Order 等
     * @param description 操作描述
     * @param beforeData  修改前数据（JSON），无则为null
     * @param afterData   修改后数据（JSON），无则为null
     * @param ipAddress   客户端IP
     * @param result      操作结果
     * @param errorMsg    错误信息
     */
    void logDetail(String module, String action, Long targetId, String targetType,
                   String description, String beforeData, String afterData,
                   String ipAddress, String result, String errorMsg);
}