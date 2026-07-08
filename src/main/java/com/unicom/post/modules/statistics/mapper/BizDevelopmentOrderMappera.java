package com.unicom.post.modules.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.post.modules.statistics.domain.entity.BizDevelopmentOrdera;
import com.unicom.post.modules.statistics.dto.StatisticsRequest;
import com.unicom.post.modules.statistics.vo.DevelopmentStatisticsVO;
import com.unicom.post.modules.statistics.vo.RankingVO;
import com.unicom.post.modules.statistics.vo.TrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
// 建议类名改成 BizDevelopmentOrderMapper（去掉末尾a）
public interface BizDevelopmentOrderMappera extends BaseMapper<BizDevelopmentOrdera> {

    /**
     * 发展量分组统计
     */
    @Select("<script>" +
            "SELECT " +
            "  <choose> " +
            "    <when test=\"req.dimension == 'CITY'\"> o.city_id AS dimensionId, c.city_name AS dimensionName </when> " +
            "    <when test=\"req.dimension == 'DISTRICT'\"> d.id AS dimensionId, d.district_name AS dimensionName </when> " +
            "    <when test=\"req.dimension == 'OUTLET'\"> o.outlet_id AS dimensionId, ot.outlet_name AS dimensionName </when> " +
            "    <when test=\"req.dimension == 'DEVELOPER'\"> o.developer_id AS dimensionId, u.real_name AS dimensionName </when> " +
            "    <when test=\"req.dimension == 'DATE'\"> DATE(o.created_at) AS dimensionId, DATE(o.created_at) AS dimensionName </when> " +
            "    <!-- 兜底分支，防止无匹配字段导致语法报错 --> " +
            "    <otherwise> DATE(o.created_at) AS dimensionId, DATE(o.created_at) AS dimensionName </otherwise> " +
            "  </choose> " +
            "  , COUNT(1) AS developmentCount " +
            "  , COUNT(1) AS orderCount " +
            "  , SUM(CASE WHEN o.business_type = 'MOBILE_CARD' THEN 1 ELSE 0 END) AS mobileCardCount " +
            "  , SUM(CASE WHEN o.business_type = 'BROADBAND' THEN 1 ELSE 0 END) AS broadbandCount " +
            "  , SUM(CASE WHEN o.business_type = 'OTHER' THEN 1 ELSE 0 END) AS otherCount " +
            "  , SUM(CASE WHEN o.lead_status = 'PROVINCE_APPROVED' THEN 1 ELSE 0 END) AS approvedCount " +
            "  , SUM(CASE WHEN o.lead_status = 'REJECTED' THEN 1 ELSE 0 END) AS rejectedCount " +
            "  , SUM(CASE WHEN o.lead_status = 'PENDING' THEN 1 ELSE 0 END) AS pendingCount " +
            "FROM biz_development_order o " +
            "LEFT JOIN sys_city c ON o.city_id = c.id " +
            "LEFT JOIN sys_district d ON o.city_id = d.city_id " +
            "LEFT JOIN biz_outlet ot ON o.outlet_id = ot.id " +
            "LEFT JOIN biz_developer dev ON o.developer_id = dev.id " +
            "LEFT JOIN sys_user u ON dev.user_id = u.id " +
            "WHERE o.is_deleted = 0 " +
            "  AND o.created_at BETWEEN #{req.startDate} AND #{req.endDate} " +
            "  <if test=\"req.cityId != null\"> AND o.city_id = #{req.cityId} </if> " +
            "  <if test=\"req.outletId != null\"> AND o.outlet_id = #{req.outletId} </if> " +
            "  <if test=\"req.businessType != null and req.businessType != ''\"> AND o.business_type = #{req.businessType} </if> " +
            "  <if test=\"dataScopeCondition != null and dataScopeCondition != ''\"> ${dataScopeCondition} </if> " +
            "GROUP BY dimensionId " +
            "ORDER BY developmentCount DESC" +
            "</script>")
    List<DevelopmentStatisticsVO> groupDevelopmentStatistics(@Param("req") StatisticsRequest req,
                                                             @Param("dataScopeCondition") String dataScopeCondition);


    /**
     * 网点/业务员排行
     */
    @Select("<script>" +
            "SELECT " +
            "  <choose> " +
            "    <when test=\"rankType == 'OUTLET'\"> o.outlet_id AS dimensionId, ot.outlet_name AS name </when> " +
            "    <when test=\"rankType == 'DEVELOPER'\"> o.developer_id AS dimensionId, u.real_name AS name </when> " +
            "    <otherwise> o.outlet_id AS dimensionId, ot.outlet_name AS name </otherwise> " +
            "  </choose> " +
            "  , COUNT(1) AS developmentCount " +
            "  , COALESCE(SUM(cd.amount), 0) AS commissionAmount " +
            "FROM biz_development_order o " +
            "LEFT JOIN biz_outlet ot ON o.outlet_id = ot.id " +
            "LEFT JOIN biz_developer dev ON o.developer_id = dev.id " +
            "LEFT JOIN sys_user u ON dev.user_id = u.id " +
            "LEFT JOIN biz_commission_detail cd ON cd.order_id = o.id AND cd.commission_phase = 'FORMAL' AND cd.status = 'SETTLED' " +
            "WHERE o.is_deleted = 0 " +
            "  AND o.created_at BETWEEN #{startDate} AND #{endDate} " +
            "  <if test=\"businessType != null and businessType != ''\"> AND o.business_type = #{businessType} </if> " +
            "  <if test=\"dataScopeCondition != null and dataScopeCondition != ''\"> ${dataScopeCondition} </if> " +
            "GROUP BY dimensionId " +
            "ORDER BY developmentCount DESC, commissionAmount DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<RankingVO> getRanking(@Param("rankType") String rankType,
                               @Param("businessType") String businessType,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate,
                               @Param("limit") Integer limit,
                               @Param("dataScopeCondition") String dataScopeCondition);

    /**
     * 趋势图数据
     */
    @Select("<script>" +
            "SELECT " +
            "  DATE_FORMAT(o.created_at, <choose>" +
            "    <when test=\"granularity == 'WEEK'\"> '%Y-%u' </when>" +
            "    <when test=\"granularity == 'MONTH'\"> '%Y-%m' </when>" +
            "    <otherwise> '%Y-%m-%d' </otherwise>" +
            "  </choose>) AS dateTime, " +
            "  <choose>" +
            "    <when test=\"metric == 'COMMISSION_AMOUNT'\"> COALESCE(SUM(cd.amount), 0) </when>" +
            "    <otherwise> COUNT(1) </otherwise>" +
            "  </choose> AS value " +
            "FROM biz_development_order o " +
            "LEFT JOIN biz_commission_detail cd ON cd.order_id = o.id AND cd.commission_phase = 'FORMAL' AND cd.status = 'SETTLED' " +
            "WHERE o.is_deleted = 0 " +
            "  AND o.created_at BETWEEN #{startDate} AND #{endDate} " +
            "  <if test=\"businessType != null and businessType != ''\"> AND o.business_type = #{businessType} </if> " +
            "  <if test=\"dataScopeCondition != null and dataScopeCondition != ''\"> ${dataScopeCondition} </if> " +
            "GROUP BY dateTime " +
            "ORDER BY dateTime ASC" +
            "</script>")
    List<TrendVO> getTrend(@Param("granularity") String granularity,
                           @Param("metric") String metric,
                           @Param("startDate") String startDate,
                           @Param("endDate") String endDate,
                           @Param("businessType") String businessType,
                           @Param("dataScopeCondition") String dataScopeCondition);
}