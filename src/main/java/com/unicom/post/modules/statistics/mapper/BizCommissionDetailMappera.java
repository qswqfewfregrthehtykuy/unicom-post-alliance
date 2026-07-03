package com.unicom.post.modules.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import com.unicom.post.modules.commission.domain.entity.BizCommissionDetail;
import com.unicom.post.modules.statistics.domain.entity.BizCommissionDetaila;
import com.unicom.post.modules.statistics.dto.StatisticsRequest;
import com.unicom.post.modules.statistics.vo.CommissionStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizCommissionDetailMappera extends BaseMapper<BizCommissionDetaila> {

    /**
     * 佣金分组统计（按维度）
     */
    @Select("<script>" +
            "SELECT " +
            "  <choose> " +
            "    <when test=\"req.dimension == 'CITY'\"> o.city_id AS dimensionId, c.city_name AS dimensionName </when> " +
            "    <when test=\"req.dimension == 'DISTRICT'\"> d.id AS dimensionId, d.district_name AS dimensionName </when> " +
            "    <when test=\"req.dimension == 'OUTLET'\"> o.outlet_id AS dimensionId, ot.outlet_name AS dimensionName </when> " +
            "    <when test=\"req.dimension == 'DEVELOPER'\"> o.developer_id AS dimensionId, u.real_name AS dimensionName </when> " +
            "    <when test=\"req.dimension == 'DATE'\"> DATE(cd.created_at) AS dimensionId, DATE(cd.created_at) AS dimensionName </when> " +
            "  </choose> " +
            "  , SUM(cd.amount) AS totalAmount " +
            "  , SUM(CASE WHEN cd.commission_phase = 'LEAD' THEN cd.amount ELSE 0 END) AS leadAmount " +
            "  , SUM(CASE WHEN cd.commission_phase = 'FORMAL' THEN cd.amount ELSE 0 END) AS formalAmount " +
            "  , SUM(CASE WHEN cd.status = 'SETTLED' THEN cd.amount ELSE 0 END) AS settledAmount " +
            "  , SUM(CASE WHEN cd.status = 'PENDING' THEN cd.amount ELSE 0 END) AS pendingAmount " +
            "FROM biz_commission_detail cd " +
            "JOIN biz_development_order o ON cd.order_id = o.id " +
            "LEFT JOIN sys_city c ON o.city_id = c.id " +
            "LEFT JOIN sys_district d ON o.city_id = d.city_id " +
            "LEFT JOIN biz_outlet ot ON o.outlet_id = ot.id " +
            "LEFT JOIN biz_developer dev ON o.developer_id = dev.id " +
            "LEFT JOIN sys_user u ON dev.user_id = u.id " +
            "WHERE cd.is_deleted = 0 AND o.is_deleted = 0 " +
            "  AND cd.created_at BETWEEN #{req.startDate} AND #{req.endDate} " +
            "  <if test=\"req.cityId != null\"> AND o.city_id = #{req.cityId} </if> " +
            "  <if test=\"req.outletId != null\"> AND o.outlet_id = #{req.outletId} </if> " +
            "  <if test=\"req.businessType != null and req.businessType != ''\"> AND o.business_type = #{req.businessType} </if> " +
            "  <if test=\"dataScopeCondition != null and dataScopeCondition != ''\"> ${dataScopeCondition} </if> " +
            "GROUP BY dimensionId " +
            "ORDER BY totalAmount DESC" +
            "</script>")
    List<CommissionStatisticsVO> groupCommissionStatistics(@Param("req") StatisticsRequest req,
                                                           @Param("dataScopeCondition") String dataScopeCondition);
}