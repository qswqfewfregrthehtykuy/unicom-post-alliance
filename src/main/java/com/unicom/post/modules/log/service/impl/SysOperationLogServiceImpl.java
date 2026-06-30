package com.unicom.post.modules.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.common.constant.AuthConstants;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.result.PageResult;
import com.unicom.post.common.utils.SecurityUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.log.domain.entity.SysOperationLog;
import com.unicom.post.modules.log.mapper.SysOperationLogMapper;
import com.unicom.post.modules.log.service.SysOperationLogService;
import com.unicom.post.modules.system.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    private final SysUserService userService;
    private final SysRoleService roleService;

    public SysOperationLogServiceImpl(SysUserService userService, SysRoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public PageResult<SysOperationLog> queryLogs(String module, String action, Long userId,
                                                 String targetType, String result,
                                                 String startDate, String endDate,
                                                 Integer pageNo, Integer pageSize) {

        // 1. 获取当前用户信息及权限
        String currentUsername = SecurityUtils.getCurrentUsername();
        SysUser currentUser = userService.getByUsername(currentUsername);
        if (currentUser == null) {
            throw new BusinessException(401, "用户未认证");
        }

        // 获取当前用户角色编码列表
        List<String> roles = roleService.findRoleCodesByUserId(currentUser.getId());
        boolean isProvince = roles.contains(AuthConstants.ROLE_PROVINCE);
        boolean isCity = roles.contains(AuthConstants.ROLE_CITY);

        // 2. 构建查询条件
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysOperationLog::getCreatedAt);

        // 模块
        if (StringUtils.hasText(module)) {
            wrapper.eq(SysOperationLog::getModule, module);
        }
        // 操作动作
        if (StringUtils.hasText(action)) {
            wrapper.eq(SysOperationLog::getAction, action);
        }
        // 目标类型
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(SysOperationLog::getTargetType, targetType);
        }
        // 操作结果
        if (StringUtils.hasText(result)) {
            wrapper.eq(SysOperationLog::getResult, result);
        }
        // 操作人ID
        if (userId != null) {
            wrapper.eq(SysOperationLog::getUserId, userId);
        }

        // 日期范围
        if (StringUtils.hasText(startDate)) {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            wrapper.ge(SysOperationLog::getCreatedAt, start.atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            wrapper.le(SysOperationLog::getCreatedAt, end.atTime(23, 59, 59));
        }

        // 3. 权限过滤：CITY 只能查看本市操作
        if (isCity && !isProvince) {
            Long cityId = currentUser.getScopeCityId();
            if (cityId == null) {
                // 城市管理员没有绑定城市，不允许查看任何日志（返回空）
                return new PageResult<>(0L, pageNo, pageSize, new ArrayList<>());
            }

            // 查找该城市下的所有用户ID
            List<Long> userIdsInCity = userService.listUserIdsByCityId(cityId);
            if (CollectionUtils.isEmpty(userIdsInCity)) {
                return new PageResult<>(0L, pageNo, pageSize, new ArrayList<>());
            }

            // 如果请求中指定了 userId，需要确保该用户属于本市，否则返回空
            if (userId != null && !userIdsInCity.contains(userId)) {
                return new PageResult<>(0L, pageNo, pageSize, new ArrayList<>());
            }

            wrapper.in(SysOperationLog::getUserId, userIdsInCity);
        }

        // 4. 分页查询
        Page<SysOperationLog> page = new Page<>(pageNo, pageSize);
        Page<SysOperationLog> pageResult = this.page(page, wrapper);

        // 5. 构造 PageResult
        return new PageResult<>(
                pageResult.getTotal(),
                Integer.valueOf((int) pageResult.getCurrent()),
                Integer.valueOf((int) pageResult.getSize()),
                pageResult.getRecords()
        );
    }
}