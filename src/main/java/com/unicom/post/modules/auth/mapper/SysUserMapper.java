package com.unicom.post.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 物理删除（绕过 @TableLogic），调用前请确保已清理关联数据 */
    @Delete("DELETE FROM sys_user WHERE id = #{userId}")
    void physicalDeleteById(@Param("userId") Long userId);
}