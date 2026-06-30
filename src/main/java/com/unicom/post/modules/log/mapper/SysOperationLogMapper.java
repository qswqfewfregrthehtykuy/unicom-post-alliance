package com.unicom.post.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.post.modules.log.domain.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}