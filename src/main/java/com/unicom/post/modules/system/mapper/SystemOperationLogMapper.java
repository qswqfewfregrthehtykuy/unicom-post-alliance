package com.unicom.post.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.post.modules.system.domain.entity.SystemOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemOperationLogMapper extends BaseMapper<SystemOperationLog> {
}