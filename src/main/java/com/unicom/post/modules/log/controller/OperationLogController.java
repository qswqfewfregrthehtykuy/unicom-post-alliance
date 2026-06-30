package com.unicom.post.modules.log.controller;

import com.unicom.post.common.result.PageResult;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.log.domain.entity.SysOperationLog;
import com.unicom.post.modules.log.service.SysOperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
public class OperationLogController {

    private final SysOperationLogService logService;

    public OperationLogController(SysOperationLogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PROVINCE', 'CITY')")
    public Result<PageResult<SysOperationLog>> listLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageResult<SysOperationLog> pageResult = logService.queryLogs(
                module, action, userId, targetType, result,
                startDate, endDate, pageNo, pageSize
        );
        return Result.success(pageResult);
    }
}