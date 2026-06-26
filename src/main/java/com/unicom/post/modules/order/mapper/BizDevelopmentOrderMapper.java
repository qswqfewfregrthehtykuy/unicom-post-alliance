package com.unicom.post.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.post.modules.order.domain.entity.BizDevelopmentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizDevelopmentOrderMapper extends BaseMapper<BizDevelopmentOrder> {

    /**
     * 查询30天内相同客户+业务类型的有效记录（用于去重校验）
     */
    @Select("SELECT * FROM biz_development_order " +
            "WHERE customer_phone = #{phone} " +
            "AND business_type = #{businessType} " +
            "AND outlet_id = #{outletId} " +
            "AND lead_status IN ('PENDING', 'OUTLET_APPROVED', 'CITY_APPROVED', 'PROVINCE_APPROVED') " +
            "AND is_deleted = 0 " +
            "AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "LIMIT 1")
    BizDevelopmentOrder findDuplicateOrder(@Param("phone") String phone,
                                           @Param("businessType") String businessType,
                                           @Param("outletId") Long outletId);
}