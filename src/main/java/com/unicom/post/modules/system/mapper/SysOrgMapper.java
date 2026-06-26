package com.unicom.post.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.post.modules.system.domain.entity.SysOrg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysOrgMapper extends BaseMapper<SysOrg> {
    @Select("SELECT * FROM sys_org WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<SysOrg> selectChildrenByParentId(Long parentId);
}