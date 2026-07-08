package com.unicom.post.modules.commission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.post.modules.commission.dto.CommissionDetailQueryDTO;
import com.unicom.post.modules.commission.domain.entity.BizCommissionDetail;
import com.unicom.post.modules.commission.vo.CommissionDetailVO;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CommissionDetailMapper extends BaseMapper<BizCommissionDetail> {

    /**
     * 分页查询佣金明细（联表获取收款方名称，并支持数据权限）
     */
    @Select("<script>" +
            "SELECT d.id, d.order_no, d.commission_phase, d.payee_type, d.payee_id, " +
            "CASE " +
            "   WHEN d.payee_type = 'OUTLET' THEN o.outlet_name " +
            "   WHEN d.payee_type = 'DEVELOPER' THEN u.real_name " +
            "   ELSE '平台留存' " +
            "END AS payee_name, " +
            "d.rule_name_snapshot, d.business_type_snapshot, d.develop_source_snapshot, " +
            "d.total_amount, d.ratio, d.amount, d.status, d.settle_at, d.created_at " +
            "FROM biz_commission_detail d " +
            "LEFT JOIN biz_outlet o ON d.payee_type = 'OUTLET' AND d.payee_id = o.id AND o.is_deleted = 0 " +
            "LEFT JOIN biz_developer bd ON d.payee_type = 'DEVELOPER' AND d.payee_id = bd.id AND bd.is_deleted = 0 " +
            "LEFT JOIN sys_user u ON bd.user_id = u.id AND u.is_deleted = 0 " +
            "LEFT JOIN biz_development_order ord ON d.order_id = ord.id AND ord.is_deleted = 0 " +
            "WHERE d.is_deleted = 0 " +
            "<if test='query.payeeType != null and query.payeeType != \"\"'> AND d.payee_type = #{query.payeeType} </if>" +
            "<if test='query.payeeId != null'> AND d.payee_id = #{query.payeeId} </if>" +
            "<if test='query.commissionPhase != null and query.commissionPhase != \"\"'> AND d.commission_phase = #{query.commissionPhase} </if>" +
            "<if test='query.status != null and query.status != \"\"'> AND d.status = #{query.status} </if>" +
            "<if test='query.cityId != null'> AND ord.city_id = #{query.cityId} </if>" +
            "<if test='query.outletId != null'> AND ord.outlet_id = #{query.outletId} </if>" +
            "<if test='query.startDate != null'> AND DATE(d.created_at) &gt;= #{query.startDate} </if>" +
            "<if test='query.endDate != null'> AND DATE(d.created_at) &lt;= #{query.endDate} </if>" +
            // 数据权限：根据当前用户角色动态添加
            "<if test='user.dataScopeType == \"CITY\"'> AND ord.city_id = #{user.scopeCityId} </if>" +
            "<if test='user.dataScopeType == \"OUTLET\"'> AND ord.outlet_id = #{user.scopeOutletId} </if>" +
            "<if test='user.dataScopeType == \"SELF\"'> AND d.payee_type = 'DEVELOPER' AND d.payee_id IN (SELECT id FROM biz_developer WHERE user_id = #{user.id} AND is_deleted = 0) </if>" +
            "ORDER BY d.created_at DESC" +
            "</script>")
    Page<CommissionDetailVO> selectCommissionDetailPage(Page<CommissionDetailVO> page,
                                                        @Param("query") CommissionDetailQueryDTO query,
                                                        @Param("user") SysUser user);

    /**
     * 汇总统计（总金额、阶段金额、状态金额、订单数、明细数）
     */
    @Select("<script>" +
            "SELECT " +
            "   SUM(d.amount) AS totalAmount, " +
            "   SUM(CASE WHEN d.commission_phase = 'LEAD' THEN d.amount ELSE 0 END) AS leadTotalAmount, " +
            "   SUM(CASE WHEN d.commission_phase = 'FORMAL' THEN d.amount ELSE 0 END) AS formalTotalAmount, " +
            "   SUM(CASE WHEN d.status = 'SETTLED' THEN d.amount ELSE 0 END) AS settledAmount, " +
            "   SUM(CASE WHEN d.status = 'PENDING' THEN d.amount ELSE 0 END) AS pendingAmount, " +
            "   SUM(CASE WHEN d.status = 'FROZEN' THEN d.amount ELSE 0 END) AS frozenAmount, " +
            "   COUNT(DISTINCT d.order_id) AS orderCount, " +
            "   COUNT(1) AS detailCount " +
            "FROM biz_commission_detail d " +
            "LEFT JOIN biz_development_order ord ON d.order_id = ord.id AND ord.is_deleted = 0 " +
            "WHERE d.is_deleted = 0 " +
            "<if test='query.payeeType != null and query.payeeType != \"\"'> AND d.payee_type = #{query.payeeType} </if>" +
            "<if test='query.payeeId != null'> AND d.payee_id = #{query.payeeId} </if>" +
            "<if test='query.commissionPhase != null and query.commissionPhase != \"\"'> AND d.commission_phase = #{query.commissionPhase} </if>" +
            "<if test='query.status != null and query.status != \"\"'> AND d.status = #{query.status} </if>" +
            "<if test='query.cityId != null'> AND ord.city_id = #{query.cityId} </if>" +
            "<if test='query.outletId != null'> AND ord.outlet_id = #{query.outletId} </if>" +
            "<if test='query.startDate != null'> AND DATE(d.created_at) &gt;= #{query.startDate} </if>" +
            "<if test='query.endDate != null'> AND DATE(d.created_at) &lt;= #{query.endDate} </if>" +
            "<if test='user.dataScopeType == \"CITY\"'> AND ord.city_id = #{user.scopeCityId} </if>" +
            "<if test='user.dataScopeType == \"OUTLET\"'> AND ord.outlet_id = #{user.scopeOutletId} </if>" +
            "<if test='user.dataScopeType == \"SELF\"'> AND d.payee_type = 'DEVELOPER' AND d.payee_id IN (SELECT id FROM biz_developer WHERE user_id = #{user.id} AND is_deleted = 0) </if>" +
            "</script>")
    SummaryBaseData selectSummaryBaseData(@Param("query") CommissionDetailQueryDTO query,
                                          @Param("user") SysUser user);

    /**
     * 按收款方类型分组汇总
     */
    @Select("<script>" +
            "SELECT d.payee_type AS payeeType, " +
            "   COUNT(DISTINCT d.payee_id) AS payeeCount, " +
            "   SUM(d.amount) AS totalAmount, " +
            "   SUM(CASE WHEN d.status = 'SETTLED' THEN d.amount ELSE 0 END) AS settledAmount " +
            "FROM biz_commission_detail d " +
            "LEFT JOIN biz_development_order ord ON d.order_id = ord.id AND ord.is_deleted = 0 " +
            "WHERE d.is_deleted = 0 " +
            "<if test='query.payeeType != null and query.payeeType != \"\"'> AND d.payee_type = #{query.payeeType} </if>" +
            "<if test='query.payeeId != null'> AND d.payee_id = #{query.payeeId} </if>" +
            "<if test='query.commissionPhase != null and query.commissionPhase != \"\"'> AND d.commission_phase = #{query.commissionPhase} </if>" +
            "<if test='query.status != null and query.status != \"\"'> AND d.status = #{query.status} </if>" +
            "<if test='query.cityId != null'> AND ord.city_id = #{query.cityId} </if>" +
            "<if test='query.outletId != null'> AND ord.outlet_id = #{query.outletId} </if>" +
            "<if test='query.startDate != null'> AND DATE(d.created_at) &gt;= #{query.startDate} </if>" +
            "<if test='query.endDate != null'> AND DATE(d.created_at) &lt;= #{query.endDate} </if>" +
            "<if test='user.dataScopeType == \"CITY\"'> AND ord.city_id = #{user.scopeCityId} </if>" +
            "<if test='user.dataScopeType == \"OUTLET\"'> AND ord.outlet_id = #{user.scopeOutletId} </if>" +
            "<if test='user.dataScopeType == \"SELF\"'> AND d.payee_type = 'DEVELOPER' AND d.payee_id IN (SELECT id FROM biz_developer WHERE user_id = #{user.id} AND is_deleted = 0) </if>" +
            "GROUP BY d.payee_type" +
            "</script>")
    List<ByPayeeItem> selectByPayeeGroup(@Param("query") CommissionDetailQueryDTO query,
                                         @Param("user") SysUser user);

    /**
     * 按阶段分组汇总
     */
    @Select("<script>" +
            "SELECT d.commission_phase AS phase, " +
            "   SUM(d.amount) AS amount, " +
            "   SUM(CASE WHEN d.status = 'SETTLED' THEN d.amount ELSE 0 END) AS settledAmount " +
            "FROM biz_commission_detail d " +
            "LEFT JOIN biz_development_order ord ON d.order_id = ord.id AND ord.is_deleted = 0 " +
            "WHERE d.is_deleted = 0 " +
            "<if test='query.payeeType != null and query.payeeType != \"\"'> AND d.payee_type = #{query.payeeType} </if>" +
            "<if test='query.payeeId != null'> AND d.payee_id = #{query.payeeId} </if>" +
            "<if test='query.commissionPhase != null and query.commissionPhase != \"\"'> AND d.commission_phase = #{query.commissionPhase} </if>" +
            "<if test='query.status != null and query.status != \"\"'> AND d.status = #{query.status} </if>" +
            "<if test='query.cityId != null'> AND ord.city_id = #{query.cityId} </if>" +
            "<if test='query.outletId != null'> AND ord.outlet_id = #{query.outletId} </if>" +
            "<if test='query.startDate != null'> AND DATE(d.created_at) &gt;= #{query.startDate} </if>" +
            "<if test='query.endDate != null'> AND DATE(d.created_at) &lt;= #{query.endDate} </if>" +
            "<if test='user.dataScopeType == \"CITY\"'> AND ord.city_id = #{user.scopeCityId} </if>" +
            "<if test='user.dataScopeType == \"OUTLET\"'> AND ord.outlet_id = #{user.scopeOutletId} </if>" +
            "<if test='user.dataScopeType == \"SELF\"'> AND d.payee_type = 'DEVELOPER' AND d.payee_id IN (SELECT id FROM biz_developer WHERE user_id = #{user.id} AND is_deleted = 0) </if>" +
            "GROUP BY d.commission_phase" +
            "</script>")
    List<ByPhaseItem> selectByPhaseGroup(@Param("query") CommissionDetailQueryDTO query,
                                         @Param("user") SysUser user);

    // 内部类用于接收汇总查询结果
    class SummaryBaseData {
        public BigDecimal totalAmount;
        public BigDecimal leadTotalAmount;
        public BigDecimal formalTotalAmount;
        public BigDecimal settledAmount;
        public BigDecimal pendingAmount;
        public BigDecimal frozenAmount;
        public Integer orderCount;
        public Integer detailCount;
    }

    class ByPayeeItem {
        public String payeeType;
        public Integer payeeCount;
        public BigDecimal totalAmount;
        public BigDecimal settledAmount;
    }

    class ByPhaseItem {
        public String phase;
        public BigDecimal amount;
        public BigDecimal settledAmount;
    }
}