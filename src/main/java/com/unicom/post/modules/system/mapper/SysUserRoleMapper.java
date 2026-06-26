package com.unicom.post.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.post.modules.system.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);

    @Insert("<script> " +
            "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
            "<foreach collection='list' item='item' separator=','> " +
            "(#{item.userId}, #{item.roleId}) " +
            "</foreach> " +
            "</script>")
    void insertBatch(@Param("list") List<SysUserRole> list);
}