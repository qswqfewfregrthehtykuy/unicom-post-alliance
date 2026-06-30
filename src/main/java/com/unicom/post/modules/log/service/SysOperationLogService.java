package com.unicom.post.modules.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.post.common.result.PageResult;
import com.unicom.post.modules.log.domain.entity.SysOperationLog;

public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询操作日志，支持权限过滤
     * @param module     模块
     * @param action     操作动作
     * @param userId     操作人ID
     * @param targetType 目标类型
     * @param result     操作结果
     * @param startDate  开始日期 (yyyy-MM-dd)
     * @param endDate    结束日期 (yyyy-MM-dd)
     * @param pageNo     页码
     * @param pageSize   每页条数
     * @return 分页结果
     */
    PageResult<SysOperationLog> queryLogs(String module, String action, Long userId,
                                          String targetType, String result,
                                          String startDate, String endDate,
                                          Integer pageNo, Integer pageSize);
}