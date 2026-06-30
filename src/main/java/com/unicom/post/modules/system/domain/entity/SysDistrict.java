package com.unicom.post.modules.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_district")
public class SysDistrict {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("city_id")
    private Long cityId;

    @TableField("district_code")
    private String districtCode;

    @TableField("district_name")
    private String districtName;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}