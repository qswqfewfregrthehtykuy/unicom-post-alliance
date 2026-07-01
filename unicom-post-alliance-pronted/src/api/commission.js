import request from '@/utils/request'

/**
 * 分页查询佣金规则列表
 * @param {Object} params
 * @param {String} params.businessType 业务类型
 * @param {String} params.developSource 发展来源
 * @param {String} params.commissionPhase 佣金阶段
 * @param {Number} params.status 状态（1启用 0停用）
 * @param {Number} params.pageNo
 * @param {Number} params.pageSize
 */
export function getCommissionRuleList(params) {
    return request({
        url: '/api/v1/commission-rules',
        method: 'get',
        params
    })
}

/**
 * 查询规则详情
 * @param {Number} ruleId
 */
export function getCommissionRuleDetail(ruleId) {
    return request({
        url: `/api/v1/commission-rules/${ruleId}`,
        method: 'get'
    })
}

/**
 * 新建佣金规则（仅省级管理员）
 * @param {Object} data
 */
export function createCommissionRule(data) {
    return request({
        url: '/api/v1/commission-rules',
        method: 'post',
        data
    })
}

/**
 * 编辑佣金规则（仅省级管理员，仅停用状态可编辑）
 * @param {Number} ruleId
 * @param {Object} data
 */
export function updateCommissionRule(ruleId, data) {
    return request({
        url: `/api/v1/commission-rules/${ruleId}`,
        method: 'put',
        data
    })
}

/**
 * 停用规则（软删除+禁用）
 * @param {Number} ruleId
 */
export function disableCommissionRule(ruleId) {
    return request({
        url: `/api/v1/commission-rules/${ruleId}`,
        method: 'delete'
    })
}

/**
 * 启用规则
 * @param {Number} ruleId
 */
export function enableCommissionRule(ruleId) {
    return request({
        url: `/api/v1/commission-rules/${ruleId}/enable`,
        method: 'put'
    })
}







/**
 * 分页查询佣金明细列表
 * @param {Object} params 查询参数
 */
export function getCommissionList(params) {
    return request({
        url: '/api/v1/commission-details',
        method: 'get',
        params
    })
}

/**
 * 获取佣金汇总统计
 * @param {Object} params 查询参数（与列表相同，不含分页）
 */
export function getSummary(params) {
    return request({
        url: '/api/v1/commission-details/summary',
        method: 'get',
        params
    })
}

/**
 * 导出佣金明细 Excel
 * @param {Object} params 查询参数
 * @returns {Promise} 返回 Blob 响应
 */
export function exportCommission(params) {
    return request({
        url: '/api/v1/commission-details/export',
        method: 'get',
        params,
        responseType: 'blob'  // 关键：接收二进制文件
    })
}